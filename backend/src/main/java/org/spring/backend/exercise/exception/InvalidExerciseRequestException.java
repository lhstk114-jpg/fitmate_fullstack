package org.spring.backend.exercise.exception;
/**
 * 요청값(muscle/equipment)이 ExerciseDB에서 지원하지 않는 값일 때.
 * IllegalArgumentException을 상속해서, 프로젝트 전역의
 * org.spring.backend.exception.GlobalExceptionHandler가 자동으로 400으로 처리하도록 함.
 */
public class InvalidExerciseRequestException extends RuntimeException {
    public InvalidExerciseRequestException(String message) {
        super(message);
    }
}
