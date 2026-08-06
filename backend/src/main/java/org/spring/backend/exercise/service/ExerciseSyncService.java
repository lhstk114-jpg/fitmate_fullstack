package org.spring.backend.exercise.service;

import java.util.List;
import java.util.Map;

/**
 * ExerciseDB(RapidAPI) 원격 데이터를 로컬 DB로 캐싱하는 동기화 서비스 인터페이스
 * 구현체: ExerciseSyncServiceImpl
 */
public interface ExerciseSyncService {

    // 특정 부위(target)의 운동 데이터를 RapidAPI에서 받아와 로컬 DB에 upsert
    void syncByTarget(String target);

    // 캐시에 해당 target 데이터가 이미 있는지 확인
    boolean isCached(String target);

    // 여러 target을 순차적으로 동기화 (관리자 전체 동기화용, target별 성공 여부를 맵으로 반환)
    Map<String, Boolean> syncAllTargets(List<String> allTargets);
}
