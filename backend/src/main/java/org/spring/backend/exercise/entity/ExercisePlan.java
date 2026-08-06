package org.spring.backend.exercise.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.spring.backend.common.BasicTime;

/**
 * 사용자가 생성한 운동 루틴 히스토리 엔티티
 * - 사용자당 최근 5개만 유지되도록 ExerciseServiceImpl.enforceHistoryLimit()에서 초과분을 삭제함
 */
@Entity
@Table(name = "exercise_plan")
@Getter
@NoArgsConstructor
public class ExercisePlan extends BasicTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private String name;   // target 영문 키 (예: "back")

    @Column(name = "name_ko")
    private String nameKo; // 화면 표시용 한글 부위명 (예: "등"). 히스토리/결과 제목에 사용.

    private String gifUrl; // 더 이상 실제 GIF를 담지 않음 (image/{id} 엔드포인트로 대체). 호환을 위해 필드만 유지.

    @Lob @Column(columnDefinition = "LONGTEXT")
    private String routine; // buildComplexRoutine 결과물 (사람이 읽는 텍스트)

    // 생성 당시의 List<ExerciseDetail>을 JSON으로 그대로 저장.
    // 히스토리에서 특정 루틴을 다시 클릭했을 때 이 값을 역직렬화해서 상세를 복원한다.
    @Lob @Column(name = "exercise_details_json", columnDefinition = "LONGTEXT")
    private String exerciseDetailsJson;

    // ⚠️ 구버전 생성자 (nameKo/exerciseDetailsJson 없이 저장) - 현재 ExerciseServiceImpl은
    // 아래의 6개 인자 생성자만 사용하고 있어 이 생성자는 미사용 상태로 보임.
    // 프로젝트 다른 곳에서 참조하는 곳이 없다면 삭제 권장 (남겨두면 nameKo/exerciseDetailsJson이
    // null인 채로 저장되는 경로가 실수로 다시 생길 여지가 있음).
    public ExercisePlan(String userEmail, String name, String gifUrl, String routine) {
        this.userEmail = userEmail;
        this.name = name;
        this.gifUrl = gifUrl;
        this.routine = routine;
    }

    /** nameKo + exerciseDetailsJson까지 함께 저장하는 생성자 (현재 실제로 사용되는 생성자) */
    public ExercisePlan(String userEmail, String name, String nameKo, String gifUrl,
                        String routine, String exerciseDetailsJson) {
        this.userEmail = userEmail;
        this.name = name;
        this.nameKo = nameKo;
        this.gifUrl = gifUrl;
        this.routine = routine;
        this.exerciseDetailsJson = exerciseDetailsJson;
    }
}
