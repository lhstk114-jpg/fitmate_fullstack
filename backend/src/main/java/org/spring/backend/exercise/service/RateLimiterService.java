package org.spring.backend.exercise.service;

/**
 * 사용자별 요청 빈도를 제한하는 레이트리미터 서비스 인터페이스
 * 구현체: RateLimiterServiceImpl (인메모리 sliding-window 방식)
 */
public interface RateLimiterService {

    /** key(보통 userEmail) 기준으로 이번 요청이 허용되는지 확인하고, 허용되면 기록도 남긴다. */
    boolean isAllowed(String key);
}
