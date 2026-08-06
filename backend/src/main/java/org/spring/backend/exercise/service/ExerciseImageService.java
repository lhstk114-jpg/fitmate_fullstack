package org.spring.backend.exercise.service;

import java.io.IOException;

/**
 * 운동 GIF 이미지를 디스크에 캐싱해 RapidAPI 호출을 최소화하는 서비스 인터페이스
 * 구현체: ExerciseImageServiceImpl
 */
public interface ExerciseImageService {

    /**
     * id + resolution에 해당하는 GIF를 반환한다.
     * 캐시에 있으면 파일에서 바로 읽고, 없으면 RapidAPI 호출 후 캐시에 저장한다.
     */
    byte[] getImage(String id, String resolution) throws IOException;
}
