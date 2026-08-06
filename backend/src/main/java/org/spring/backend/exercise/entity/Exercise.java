package org.spring.backend.exercise.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ExerciseDB(RapidAPI) 응답을 로컬에 캐싱하기 위한 엔티티.
 * id는 ExerciseDB가 내려주는 exerciseId를 그대로 PK로 사용한다
 * (예: "0001") -> 동일 운동 재동기화 시 덮어쓰기(upsert) 가능.
 */
@Entity
@Table(name = "exercise", indexes = {
        @Index(name = "idx_exercise_target", columnList = "target"),
        @Index(name = "idx_exercise_equipment", columnList = "equipment")
})
@Setter
@Getter
@NoArgsConstructor
public class Exercise {

    @Id
    private String id; // ExerciseDB의 exerciseId

    private String name;
    private String target;      // 주동 근육
    private String bodyPart;    // 대분류 부위
    private String equipment;
    private String gifUrl;      // 현재는 실사용 안 함 - GIF는 /api/exercise/image/{id}로 별도 조회 (ExerciseImageService)

    // 아래 4개 컬럼은 ExerciseTranslationService가 채워주는 한글 번역 필드 (초기 동기화 직후엔 전부 null)
    @Column(name = "name_ko")
    private String nameKo;

    @Column(name = "target_ko")
    private String targetKo;

    @Column(name = "equip_ko")
    private String equipKo;

    @Column(name = "body_ko")
    private String bodyKo;

    // 신규 저장용 생성자 (ExerciseSyncServiceImpl에서 캐시에 없는 새 운동을 저장할 때 사용)
    public Exercise(String id, String name, String target, String bodyPart, String equipment, String gifUrl) {
        this.id = id;
        this.name = name;
        this.target = target;
        this.bodyPart = bodyPart;
        this.equipment = equipment;
        this.gifUrl = gifUrl;
    }

    /** 동기화 시 필드 갱신용 (재호출 시 최신 데이터로 덮어쓰기, id/target은 그대로 유지) */
    public void update(String name, String bodyPart, String equipment, String gifUrl) {
        this.name = name;
        this.bodyPart = bodyPart;
        this.equipment = equipment;
        this.gifUrl = gifUrl;
    }
}
