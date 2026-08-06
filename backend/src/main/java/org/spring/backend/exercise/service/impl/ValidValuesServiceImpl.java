package org.spring.backend.exercise.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.exercise.entity.Exercise;
import org.spring.backend.exercise.ExerciseDbClient;
import org.spring.backend.exercise.repository.ExerciseRepository;
import org.spring.backend.exercise.service.ValidValuesService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * ValidValuesService 구현체
 * RapidAPI ExerciseDB가 제공하는 "유효한 target/equipment 값 목록"을
 * 앱 시작 시 한 번 받아와 캐싱한다. (하드코딩 대신 API가 제공하는 목록을 신뢰)
 *
 * 앱 기동 시 API 호출이 실패하면(레이트리밋 등) 빈 캐시로 남는데,
 * 이 경우 isValidTarget/isValidEquipment는 "검증 불가 상태"로 보고
 * 통과시킨다 (서비스 전체가 막히는 것보다 낫다는 판단).
 *
 * ✅ 정리: 예전에는 target/equipment의 한글 번역을 이 서비스가 별도 맵(targetKoMap/equipKoMap)에
 *    캐싱해서 썼던 것으로 보이나, 지금은 한글 번역이 DB(Exercise.targetKo/equipKo 컬럼)에
 *    저장되어 있고 getTargetMap()/getEquipMap()이 매번 DB에서 직접 조회하는 방식으로 바뀌어
 *    그 두 캐시 맵은 더 이상 쓰이지 않아 삭제함.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidValuesServiceImpl implements ValidValuesService {

    private final ExerciseDbClient exerciseDbClient;
    private final ExerciseRepository exerciseRepository;

    // 앱 기동 시 로드되는 유효한 target/equipment 값 캐시 (동시성 안전을 위해 ConcurrentHashMap 기반 Set 사용)
    private final Set<String> validTargets = ConcurrentHashMap.newKeySet();
    private final Set<String> validEquipments = ConcurrentHashMap.newKeySet();

    @Override
    public Map<String, String> getTargetMap() {
        Map<String, String> map = new LinkedHashMap<>();
        // DB에서 고유한 target과 targetKo를 조회하여 맵 구성
        List<Exercise> exercises = exerciseRepository.findAll();
        for (Exercise ex : exercises) {
            if (ex.getTarget() != null) {
                String ko = (ex.getTargetKo() != null && !ex.getTargetKo().isBlank()) ? ex.getTargetKo() : ex.getTarget();
                map.put(ex.getTarget().toLowerCase(), ko);
            }
        }
        return map;
    }

    @Override
    public Map<String, String> getEquipMap() {
        Map<String, String> map = new LinkedHashMap<>();
        List<Exercise> exercises = exerciseRepository.findAll();
        for (Exercise ex : exercises) {
            if (ex.getEquipment() != null) {
                String ko = (ex.getEquipKo() != null && !ex.getEquipKo().isBlank()) ? ex.getEquipKo() : ex.getEquipment();
                map.put(ex.getEquipment().toLowerCase(), ko);
            }
        }
        return map;
    }

    // 앱 기동 시 1회 실행: ExerciseDB의 유효 target/equipment 목록을 받아와 캐싱
    @PostConstruct
    public void loadValidValues() {
        loadList(exerciseDbClient::getTargetList, validTargets, "target");
        loadList(exerciseDbClient::getEquipmentList, validEquipments, "equipment");
    }

    // API 응답(JsonNode 배열)을 소문자로 정규화해 Set에 채우는 공통 로직, 실패 시 로그만 남기고 빈 캐시로 남음
    private void loadList(Supplier<JsonNode> fetcher, Set<String> target, String label) {
        try {
            JsonNode response = fetcher.get();
            if (response != null && response.isArray()) {
                response.forEach(node -> target.add(node.asText().toLowerCase()));
                log.info("ValidValuesService: {} 목록 {}건 로드", label, target.size());
            }
        } catch (RestClientResponseException e) {
            log.warn("ValidValuesService: {} 목록 로드 실패 (status={}). 검증 없이 통과시킵니다.",
                    label, e.getStatusCode());
        } catch (Exception e) {
            log.warn("ValidValuesService: {} 목록 로드 중 예외 발생: {}", label, e.getMessage());
        }
    }

    @Override
    public boolean isValidTarget(String target) {
        if (validTargets.isEmpty()) return true; // 로드 실패 시 검증 스킵
        return validTargets.contains(target.toLowerCase());
    }

    @Override
    public boolean isValidEquipment(String equipment) {
        if (validEquipments.isEmpty()) return true; // 로드 실패 시 검증 스킵
        return validEquipments.contains(equipment.toLowerCase());
    }

    @Override
    public Set<String> getValidTargets() {
        return Set.copyOf(validTargets);
    }

    @Override
    public List<String> getAllValidTargets(){return List.copyOf(validTargets);}

    @Override
    public Set<String> getValidEquipments() {
        return Set.copyOf(validEquipments);
    }
}
