package org.spring.backend.exercise.service;

/**
 * exercise 테이블의 한글 컬럼(name_ko, target_ko, equip_ko, body_ko)이 비어있는 행들을
 * Google Cloud Translation API로 채워주는 배치 서비스 인터페이스
 * 구현체: ExerciseTranslationServiceImpl
 */
public interface ExerciseTranslationService {

    /**
     * name_ko가 NULL인 운동들을 찾아 name/target/equipment/bodyPart를 전부 번역해서 채운다.
     * @return 번역에 성공한 운동 개수
     */
    int translateMissingNames();
}
