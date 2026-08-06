package org.spring.backend.exercise.service.impl;

import org.spring.backend.exercise.service.RateLimiterService;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimiterService 구현체
 * 사용자별 /recommend 호출을 제한하는 인메모리 sliding-window 레이트리미터.
 *
 * 주의: 서버 인스턴스가 여러 대(수평 확장)면 이 방식은 인스턴스별로 따로 카운트되어
 * 실질적인 제한이 느슨해진다. 이미 프로젝트에서 Redis를 쓰고 있다면
 * (기존 GlobalExceptionHandler의 IllegalStateException 주석에 Redis 언급이 있어서) 나중에
 * RedisTemplate 기반 INCR+EXPIRE 방식으로 바꾸는 걸 권장. 지금은 단일 인스턴스 가정하에
 * 간단하게 구현.
 */
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    // 1분에 5회까지만 허용
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000; // 1분

    // key(userEmail)별로 최근 요청 시각들을 큐에 기록해두고, 윈도우 밖으로 나간 것들을 지워가며 카운트
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // 현재 시각 기준으로 윈도우(1분)를 벗어난 오래된 기록은 앞에서부터 제거
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            // 윈도우 내 요청 수가 이미 최대치면 거부
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            // 허용되는 경우 이번 요청 시각을 기록하고 통과
            timestamps.addLast(now);
            return true;
        }
    }
}
