package org.spring.backend.exercise.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.exercise.ExerciseDbClient;
import org.spring.backend.exercise.service.ExerciseImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ExerciseImageService 구현체
 * exercise GIF를 디스크에 캐싱해서, 같은 운동의 이미지는
 * 최초 1회만 RapidAPI를 호출하고 이후엔 전부 로컬 파일에서 서빙한다.
 * RapidAPI 크레딧/호출 한도 보호가 목적 - 화면에 운동이 반복해서
 * 표시될 때마다 매번 API를 부르면 순식간에 소진되므로,
 * 여기서 한 번 받은 GIF는 서버가 영구 저장해서 재사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseImageServiceImpl implements ExerciseImageService {

    private final ExerciseDbClient exerciseDbClient;

    // GIF 캐시 저장 디렉토리 (application.yml의 img.path.exercise)
    @Value("${img.path.exercise}")
    private String cacheDir;

    @Override
    public byte[] getImage(String id, String resolution) throws IOException {
        Path filePath = resolveCachePath(id, resolution);

        if (Files.exists(filePath)) {
            log.debug("캐시 히트: {}", filePath);
            return Files.readAllBytes(filePath);
        }

        log.info("캐시 미스 - RapidAPI 호출: id={}, resolution={}", id, resolution);
        byte[] gifBytes = exerciseDbClient.getImage(id, resolution);

        saveToCache(filePath, gifBytes);
        return gifBytes;
    }

    // 캐시 파일 경로 계산 (id에 파일명으로 못 쓰는 문자가 섞여있을 가능성을 대비해 안전하게 치환)
    private Path resolveCachePath(String id, String resolution) {
        String safeId = id.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = safeId + "_" + resolution + ".gif";
        return Paths.get(cacheDir, fileName);
    }

    // 받아온 GIF를 디스크에 저장 (저장 실패해도 이번 요청 응답 자체는 이미 성공했으므로 예외를 던지지 않고 로그만 남김)
    private void saveToCache(Path filePath, byte[] gifBytes) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, gifBytes);
            log.debug("캐시 저장 완료: {}", filePath);
        } catch (IOException e) {
            log.warn("캐시 저장 실패 (다음 요청 때 재시도됨): {}", filePath, e);
        }
    }
}
