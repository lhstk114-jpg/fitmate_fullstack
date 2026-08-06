package org.spring.backend.exercise.exception;
/**
 * 조건에 맞는 운동 데이터를 찾지 못했을 때 (값 자체는 유효하지만 결과가 없는 경우).
 * 이것도 결국 "이 조건으로는 결과를 줄 수 없다"는 클라이언트 요청 문제로 보고
 * 동일하게 IllegalArgumentException을 상속해 400으로 처리되도록 함.
 */
public class ExerciseNotFoundException extends RuntimeException {
    public ExerciseNotFoundException(String message) {
        super(message);
    }
}
