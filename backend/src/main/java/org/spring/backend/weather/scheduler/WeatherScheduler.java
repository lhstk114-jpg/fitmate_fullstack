package org.spring.backend.weather.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.weather.service.impl.WeatherServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CommunityMain.jsx에서 쓰는 도시를 1시간마다 미리 갱신해둔다.
 * ⚠️ 이 목록은 프론트 CITIES 배열의 code 값과 반드시 동일하게 유지할 것.
 * 새 도시를 추가/삭제하면 여기도 같이 수정해야 함.
 *
 * 목적: 사용자가 CommunityMain 페이지에 접속했을 때 매번 OpenWeather API를 직접 호출하지 않고,
 * 미리 캐시된(최대 1시간 이내) 날씨 데이터를 바로 내려줄 수 있도록 백그라운드에서 선갱신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    // WeatherService 인터페이스가 아니라 구현체를 직접 주입 - forceRefresh가
    // 인터페이스에는 없는 스케줄러 전용 메서드라서. 인터페이스도 수정 가능하면
    // 그쪽에 refreshWeather(String) 등으로 정식 노출하는 게 더 깔끔할 수 있음.
    private final WeatherServiceImpl weatherService;

    // 스케줄러가 매시간 갱신할 도시 코드 목록 (프론트 CommunityMain.jsx의 CITIES와 동기화 필요)
    private static final List<String> CITY_CODES = List.of(
            "Seoul", "Suwon", "Cheonan", "Cheongju", "Jeonju",
            "Gwangju", "Pohang", "Changwon", "Chuncheon", "Jeju"
    );

    /** 매 정시(1:00, 2:00, 3:00 ...)에 실행 */
    @Scheduled(cron = "0 0 * * * *")
    public void refreshAllCities() {
        log.info("날씨 캐시 갱신 시작: {}개 도시", CITY_CODES.size());
        for (String city : CITY_CODES) {
            try {
                // forceRefresh: 기존 캐시가 TTL 이내라도 무시하고 무조건 최신 데이터로 갱신
                weatherService.forceRefresh(city);
            } catch (Exception e) {
                // 한 도시 갱신 실패가 나머지 도시 갱신을 막지 않도록 개별적으로 예외 처리
                log.warn("날씨 갱신 실패: city={}", city, e);
            }
        }
        log.info("날씨 캐시 갱신 완료");
    }
}
