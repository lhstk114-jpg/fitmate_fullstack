package org.spring.backend.exception;

/** 사용자가 짧은 시간에 너무 많이 요청했을 때 (429 Too Many Requests) */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
