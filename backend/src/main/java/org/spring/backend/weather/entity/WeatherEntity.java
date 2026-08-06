package org.spring.backend.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 도시별 날씨 캐시 엔티티
 * - 도시 코드(city)를 PK로 사용해 도시당 1행만 유지 (매번 새로 insert하지 않고 update)
 * - OpenWeather API 호출 결과를 캐싱해 TTL(WeatherServiceImpl의 CACHE_TTL_MINUTES) 동안 재사용
 * - fetchTime 기준으로 캐시가 신선한지(isFresh) 판단
 */
@Entity
@Table(name = "weather")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherEntity {
    // 도시 코드 (예: "Seoul", "Suwon" 등, 프론트 CITIES 배열의 code 값과 동일해야 함)
    @Id
    private String city;

    // 현재 기온 (섭씨)
    private Double temp;

    // 날씨 아이콘 코드 (OpenWeather 아이콘 URL 조합에 사용)
    private String icon;

    // 날씨 설명 (한글, 예: "구름 조금")
    private String description;

    // 이 데이터를 마지막으로 API에서 가져온 시각 (캐시 신선도 판단 기준)
    private LocalDateTime fetchTime;


    // 기존 캐시 행을 최신 값으로 갱신 (매번 새 엔티티를 만들지 않고 재사용)
    public void update(Double temp, String icon, String description, LocalDateTime fetchTime){
        this.temp = temp;
        this.icon = icon;
        this.description = description;
        this.fetchTime=fetchTime;
    }
}
