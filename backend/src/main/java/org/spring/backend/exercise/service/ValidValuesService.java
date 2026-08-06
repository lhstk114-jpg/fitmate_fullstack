package org.spring.backend.exercise.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RapidAPI ExerciseDB가 제공하는 "유효한 target/equipment 값 목록"을 캐싱해두고
 * 요청 검증(isValidTarget/isValidEquipment) 및 프론트 드롭다운 구성에 사용하는 서비스 인터페이스
 * 구현체: ValidValuesServiceImpl
 */
public interface ValidValuesService {

    // DB에 저장된 고유 target 값들을 { 영문키: 한글명 } 형태로 반환 (프론트 드롭다운용)
    Map<String, String> getTargetMap();

    // DB에 저장된 고유 equipment 값들을 { 영문키: 한글명 } 형태로 반환
    Map<String, String> getEquipMap();

    // target 값이 ExerciseDB가 지원하는 유효한 값인지 확인
    boolean isValidTarget(String target);

    // equipment 값이 ExerciseDB가 지원하는 유효한 값인지 확인
    boolean isValidEquipment(String equipment);

    // 유효한 target 전체 집합 (관리자 전체 동기화 등에서 순회할 때 사용)
    Set<String> getValidTargets();

    List<String> getAllValidTargets();

    Set<String> getValidEquipments();
}
