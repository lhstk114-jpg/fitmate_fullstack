package org.spring.backend.exercise.repository;
import org.spring.backend.exercise.entity.ExercisePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자별 운동 루틴 히스토리(ExercisePlan) 리포지토리
 */
@Repository
public interface ExercisePlanRepository extends JpaRepository<ExercisePlan, Long> {

    // 최근 생성 순으로 특정 사용자의 루틴 히스토리를 페이지 단위로 조회 (HistoryList.jsx, /api/exercise/history)
    Page<ExercisePlan> findByUserEmailOrderByIdDesc(String userEmail, Pageable pageable);

    // 보관 개수 체크용 (ExerciseServiceImpl.enforceHistoryLimit에서 전체 조회 후 초과분 판단)
    //보관 갯수 체크용
    List<ExercisePlan> findByUserEmailOrderByIdDesc(String userEmail);

}
