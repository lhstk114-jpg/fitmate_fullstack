package org.spring.backend.weather.repository;

import org.spring.backend.weather.entity.WeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 날씨 캐시 엔티티에 대한 JPA 리포지토리
 * - PK 타입이 String(도시 코드)이므로 JpaRepository<WeatherEntity, String>
 * - findById(city)로 해당 도시의 캐시된 날씨를 조회하는 데 주로 사용됨 (WeatherServiceImpl 참고)
 */
public interface WeatherRepository extends JpaRepository<WeatherEntity, String> {
}
