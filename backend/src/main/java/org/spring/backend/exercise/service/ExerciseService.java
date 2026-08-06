package org.spring.backend.exercise.service;

import org.spring.backend.exercise.dto.ExerciseDetail;
import org.spring.backend.exercise.entity.ExercisePlan;

import java.util.List;
import java.util.Map;

/**
 * 운동 루틴 생성/추천/히스토리 서비스 인터페이스
 * 구현체: ExerciseServiceImpl
 */
public interface ExerciseService {

    // 특정 부위(target)에 해당하는 장비 목록만 추출 (RoutineForm.jsx의 장비 select용)
    Map<String, String> getEquipmentsMapByTarget(String target);

    // 부위+장비 조건으로 새 루틴을 생성하고 히스토리에 저장 (부위별 세트/렙/휴식 규칙 적용)
    RoutineResult generateRoutine(String userEmail, String muscle, String equipment);

    // 로그인 사용자의 최근 루틴 3개에 등장한 운동을 제외하고, 캐싱된 운동 중 5개를 무작위로 추천 (저장/API호출 없음)
    List<ExerciseDetail> personalizedQuickPick(String userEmail);

    // 사용자의 루틴 히스토리를 최신순으로 페이지 조회
    List<ExercisePlan> getHistory(String userEmail, int page, int size);

    /** 컨트롤러에서 plan + 상세 목록을 함께 응답으로 변환할 수 있도록 감싸는 결과 객체 */
    record RoutineResult(ExercisePlan plan, List<ExerciseDetail> details) {}
}
