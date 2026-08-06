package org.spring.backend.exercise.dto;

import lombok.*;

/**
 * 루틴 결과에 표시되는 운동 1건의 상세 정보 (세트/렙/휴식이 부여된 형태)
 * @NoArgsConstructor + @Setter 조합은 Jackson이 JSON <-> 객체 변환을 할 수 있게 하기 위함
 * (ExercisePlan.exerciseDetailsJson에 저장/복원할 때 필요). @Builder는 서비스 코드에서
 * 기존처럼 빌더 스타일로 생성할 수 있게 그대로 유지.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseDetail {
    private String id;         // ExerciseDB의 exerciseId - 프론트가 /api/exercise/image/{id}로 GIF 지연 로딩할 때 사용
    private String name;       // 운동 이름 (한글 번역 있으면 한글, 없으면 영문 폴백)
    private String target;     // 주동 근육 (한글/영문 폴백 동일)
    private String equipment;  // 사용 장비 (한글/영문 폴백 동일)
    private int sets;          // 세트 수
    private String reps;       // 반복 횟수 (문자열: "8-12" 같은 범위 표현도 가능하도록)
    private int restSeconds;   // 세트 간 휴식 시간(초)
}
