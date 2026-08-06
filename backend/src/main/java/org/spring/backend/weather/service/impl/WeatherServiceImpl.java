package org.spring.backend.weather.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.weather.service.WeatherService;
import org.spring.backend.weather.entity.WeatherEntity;
import org.spring.backend.weather.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 날씨 조회 서비스 구현체
 * - DB 캐시(WeatherEntity) 우선 조회 → TTL(2시간) 이내면 그대로 반환
 * - 캐시가 없거나 오래됐으면 OpenWeather API를 호출해 갱신 후 반환
 * - API 호출이 실패하면 오래된 캐시라도 있으면 그거라도 폴백으로 반환 (완전히 빈 응답보다 낫다는 판단)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final RestClient restClient;
    private final WeatherRepository weatherRepository;

    // OpenWeather API 인증키 (application.yml/properties의 openweather.api-key)
    @Value("${openweather.api-key}")
    private String apiKey;

    // OpenWeather API 기본 URL
    @Value("${openweather.base-url}")
    private String baseUrl;

    // 캐시 유효 시간 (분 단위) - 이 시간 이내에 조회된 데이터면 API를 다시 호출하지 않고 캐시를 그대로 사용
    private static final long CACHE_TTL_MINUTES = 120; // 2시간

    /**
     * 일반 조회: 캐시가 있고 신선하면(isFresh) 캐시 그대로 반환,
     * 없거나 오래됐으면 API를 새로 호출해서 캐시 갱신 후 반환 (fetchAndCache)
     */
    @Override
    public Map<String, Object> getWeather(String city) {
        return weatherRepository.findById(city)
                .filter(this::isFresh)
                .map(this::toResponseMap)
                .orElseGet(() -> fetchAndCache(city));
    }

    /** 스케줄러 전용 - TTL과 무관하게 무조건 최신으로 갱신 */
    public void forceRefresh(String city) {
        fetchAndCache(city);
    }

    // 캐시 엔티티의 fetchTime이 TTL(2시간) 이내인지 판단
    private boolean isFresh(WeatherEntity entity) {
        return entity.getFetchTime() != null
                && entity.getFetchTime().isAfter(LocalDateTime.now().minusMinutes(CACHE_TTL_MINUTES));
    }

    /**
     * OpenWeather API를 호출해 최신 날씨를 가져오고 DB 캐시를 갱신(update 또는 신규 저장)한 뒤 응답 형태로 변환
     * API 호출이 실패하면, DB에 남아있는 캐시가 있으면(오래됐더라도) 그거라도 반환하고 없으면 null 반환
     */
    private Map<String, Object> fetchAndCache(String city) {
        Map<String, Object> raw = callOpenWeather(city);

        if (raw != null) {
            SlimWeather slim = extractSlim(raw);
            // 기존 캐시 행이 있으면 값만 갱신(update), 없으면 신규 저장
            weatherRepository.findById(city).ifPresentOrElse(
                    existing -> {
                        existing.update(slim.temp(), slim.icon(), slim.description(), LocalDateTime.now());
                        weatherRepository.save(existing);
                    },
                    () -> weatherRepository.save(
                            new WeatherEntity(city, slim.temp(), slim.icon(), slim.description(), LocalDateTime.now()))
            );
            return toResponseMap(slim);
        }

        // API 호출 실패 시, 오래된 캐시라도 있으면 그거라도 돌려준다
        log.warn("날씨 API 호출 실패, 캐시(있다면 오래된 것)로 폴백: city={}", city);
        return weatherRepository.findById(city)
                .map(this::toResponseMap)
                .orElse(null);
    }

    /** 프론트가 기대하는 { main: { temp }, weather: [{ icon, description }] } 형태로 재조립 (엔티티 버전) */
    private Map<String, Object> toResponseMap(WeatherEntity entity) {
        return toResponseMap(new SlimWeather(entity.getTemp(), entity.getIcon(), entity.getDescription()));
    }

    /** 프론트가 기대하는 { main: { temp }, weather: [{ icon, description }] } 형태로 재조립 (SlimWeather 버전) */
    private Map<String, Object> toResponseMap(SlimWeather slim) {
        return Map.of(
                "main", Map.of("temp", slim.temp()),
                "weather", List.of(Map.of(
                        "icon", slim.icon(),
                        "description", slim.description()
                ))
        );
    }

    // OpenWeather API 원본 응답(Map)에서 필요한 필드(온도/아이콘/설명)만 추출
    @SuppressWarnings("unchecked")
    private SlimWeather extractSlim(Map<String, Object> raw) {
        Map<String, Object> mainMap = (Map<String, Object>) raw.get("main");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) raw.get("weather");
        Map<String, Object> weatherFirst = (weatherList != null && !weatherList.isEmpty())
                ? weatherList.get(0) : Map.of();

        Double temp = (mainMap != null && mainMap.get("temp") instanceof Number n) ? n.doubleValue() : null;
        String icon = (String) weatherFirst.getOrDefault("icon", "");
        String description = (String) weatherFirst.getOrDefault("description", "");

        return new SlimWeather(temp, icon, description);
    }

    // OpenWeather API 실제 호출 (도시명, 섭씨 단위, 한글 설명으로 요청). 실패 시 예외를 삼키고 null 반환
    private Map<String, Object> callOpenWeather(String city) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "kr")
                    .toUriString();

            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.error(
                    "날씨 조회 실패: city={}, baseUrl={}, apiKeyExists={}, message={}",
                    city,
                    baseUrl,
                    apiKey != null && !apiKey.isBlank(),
                    e.getMessage(),
                    e
            );
            return null;
        }
    }

    // API 응답 중 실제로 필요한 값만 담는 내부 임시 레코드 (temp/icon/description)
    private record SlimWeather(Double temp, String icon, String description) {}
}
