package org.spring.backend.exercise.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.exercise.entity.Exercise;
import org.spring.backend.exercise.ExerciseDbClient;
import org.spring.backend.exercise.repository.ExerciseRepository;
import org.spring.backend.exercise.service.ExerciseSyncService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ExerciseSyncService 구현체
 * 무료 티어의 빡빡한 rate limit 때문에, 요청마다 API를 호출하지 않고
 * "부위별로 캐시에 없을 때만" 1회 동기화 후 이후에는 로컬 DB만 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSyncServiceImpl implements ExerciseSyncService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseDbClient exerciseDbClient;

    @Override
    public void syncByTarget(String target) {
        JsonNode response;
        try {
            response = exerciseDbClient.getExercisesByTarget(target);
        } catch (RestClientResponseException e) {
            // RapidAPI 레이트리밋(429) 등 응답 실패 시, 예외를 위로 던지지 않고
            // 있는 캐시라도 쓸 수 있게 로그만 남기고 조용히 종료한다.
            log.warn("ExerciseDB sync 실패 (target={}, status={}): {}",
                    target, e.getStatusCode(), e.getMessage());
            return;
        }

        if (response == null || !response.isArray()) {
            log.warn("ExerciseDB sync: target={} 응답이 비어있거나 배열이 아닙니다.", target);
            return;
        }

        List<Exercise> toSave = new ArrayList<>();
        for (JsonNode node : response) {
            String id = node.path("id").asText(null);
            if (id == null) continue;

            String name = node.path("name").asText("");
            String bodyPart = node.path("bodyPart").asText("");
            String equipment = node.path("equipment").asText("");
            String gifUrl = node.path("gifUrl").asText("");

            // 이미 있는 운동이면 최신 값으로 갱신(update), 없으면 신규 목록에 추가해 나중에 한 번에 저장
            exerciseRepository.findById(id).ifPresentOrElse(
                    existing -> existing.update(name, bodyPart, equipment, gifUrl),
                    () -> toSave.add(new Exercise(id, name, target, bodyPart, equipment, gifUrl))
            );
        }

        if (!toSave.isEmpty()) {
            exerciseRepository.saveAll(toSave);
        }
        log.info("ExerciseDB sync 완료: target={}, 신규 {}건 저장", target, toSave.size());
    }

    @Override
    public boolean isCached(String target) {
        return exerciseRepository.existsByTargetIgnoreCase(target);
    }

    @Override
    public Map<String, Boolean> syncAllTargets(List<String> allTargets) {
        Map<String, Boolean> result = new LinkedHashMap<>();

        for (String target : allTargets) {
            try {
                syncByTarget(target);
                result.put(target, true);
                log.info("동기화 성공: {}", target);
            } catch (Exception e) {
                // RapidAPI 일일 한도 초과 등으로 하나 실패해도
                // 나머지 target은 계속 진행되도록 함
                result.put(target, false);
                log.error("동기화 실패: {}", target, e);
            }

            try {
                // target 간 호출 간격 (RapidAPI 초당 호출 제한 보호)
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }

        return result;
    }
}
