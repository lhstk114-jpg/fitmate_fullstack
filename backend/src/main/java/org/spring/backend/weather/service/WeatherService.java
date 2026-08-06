package org.spring.backend.weather.service;

import java.util.Map;

/**
 * 날씨 조회 서비스 인터페이스
 * 프론트(CommunityMain.jsx)가 기대하는 형태({ main: { temp }, weather: [{ icon, description }] })의
 * Map을 반환하는 getWeather(city) 하나만 정의되어 있음
 *
 * 참고: 스케줄러(WeatherScheduler)가 사용하는 forceRefresh(city)는 이 인터페이스에는 없고
 * 구현체(WeatherServiceImpl)에만 존재함 (인터페이스 분리 여부는 팀 컨벤션에 맞게 조정 가능)
 */
public interface WeatherService {
    public Map<String, Object> getWeather(String city);
}
