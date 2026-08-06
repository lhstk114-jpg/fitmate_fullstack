package org.spring.backend.exercise.repository;

import org.spring.backend.exercise.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 운동(Exercise) 리포지토리 - PK가 String(ExerciseDB의 exerciseId)임에 유의
 */
@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, String> {

    // 부위 + 장비 조건이 둘 다 있을 때
    List<Exercise> findByTargetIgnoreCaseAndEquipmentIgnoreCase(String target, String equipment);

    // 부위만으로 조회 (equipment 미지정/캐시 부족 시 폴백용)
    List<Exercise> findByTargetIgnoreCase(String target);

    // 해당 부위가 이미 한 번이라도 캐싱되어 있는지 확인 (ExerciseSyncServiceImpl.isCached)
    boolean existsByTargetIgnoreCase(String target);

    // 스케줄러가 "이미 한 번 캐싱된 부위"만 재동기화하기 위해 조회
    // ⚠️ 현재 이 메서드를 사용하는 ExerciseSyncScheduler가 전체 주석 처리되어 있어 실질적으로 미사용 상태.
    //    스케줄러를 쓰지 않기로 하면 이 메서드도 함께 정리 대상.
    @Query("SELECT DISTINCT e.target FROM Exercise e")
    List<String> findDistinctTargets();

    // 번역 배치(ExerciseTranslationServiceImpl)가 대상을 찾을 때 사용
    List<Exercise> findByNameKoIsNull();
}
