package org.spring.backend.exercise.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.exercise.entity.Exercise;
import org.spring.backend.exercise.repository.ExerciseRepository;
import org.spring.backend.exercise.service.ExerciseTranslationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExerciseTranslationService 구현체
 *
 * 핵심 최적화:
 * - name은 운동마다 다 달라서 개별 번역이 필요하지만,
 *   target/equipment/bodyPart는 전체 종류가 30~40개뿐이라
 *   운동 개수만큼 반복 번역하지 않고 "고유값별로 딱 한 번만" 번역해서 캐싱한 뒤 재사용한다.
 *   (1,300개 운동 x 3개 필드를 매번 새로 번역하면 이론상 3,900번 호출해야 하지만,
 *    이 방식으로는 실제로는 name 1,300번 + target/equip/body 고유값 합쳐 30~40번 정도로 끝남)
 *
 * sync(RapidAPI 동기화)와는 완전히 분리되어 있어서,
 * recommend/history 요청 시에는 절대 호출되지 않는다 (DB에 저장된 값만 읽음).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseTranslationServiceImpl implements ExerciseTranslationService {

    private final ExerciseRepository exerciseRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.translate.api-key}")
    private String apiKey;

    private static final String TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2";

    @Override
    public int translateMissingNames() {
        List<Exercise> targets = exerciseRepository.findByNameKoIsNull();
        log.info("번역 대상 {}건 조회됨", targets.size());

        // target/equipment/bodyPart는 고유값이 적으므로 "값 -> 번역결과" 캐시를 만들어 재사용
        Map<String, String> translationCache = new HashMap<>();

        int successCount = 0;

        for (Exercise exercise : targets) {
            try {
                String nameKo = translate(exercise.getName());               // name은 매번 다르므로 개별 번역
                String targetKo = translateCached(exercise.getTarget(), translationCache);
                String equipKo = translateCached(exercise.getEquipment(), translationCache);
                String bodyKo = translateCached(exercise.getBodyPart(), translationCache);

                exercise.setNameKo(nameKo);
                exercise.setTargetKo(targetKo);
                exercise.setEquipKo(equipKo);
                exercise.setBodyKo(bodyKo);
                exerciseRepository.save(exercise);
                successCount++;
            } catch (Exception e) {
                // 한 건 실패해도 전체 배치가 멈추지 않도록 로그만 남기고 계속 진행
                log.error("번역 실패 - exerciseId={}, name={}",
                        exercise.getId(), exercise.getName(), e);
            }
        }

        log.info("번역 완료: {}/{}건 성공 (캐시 히트로 절약된 target/equip/body 번역 호출 다수)",
                successCount, targets.size());
        return successCount;
    }

    /**
     * 캐시에 있으면 재사용, 없으면 번역 후 캐시에 저장.
     * target/equipment/bodyPart처럼 반복되는 값에만 사용.
     */
    private String translateCached(String text, Map<String, String> cache) {
        if (text == null) return null;
        return cache.computeIfAbsent(text, this::translate);
    }

    /**
     * 단일 문자열을 Google Cloud Translation API로 영->한 번역.
     */
    private String translate(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "q", text,
                "source", "en",
                "target", "ko",
                "format", "text"
        );
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        String url = UriComponentsBuilder.fromHttpUrl(TRANSLATE_URL)
                .queryParam("key", apiKey)
                .toUriString();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        // 응답 구조: { "data": { "translations": [ { "translatedText": "..." } ] } }
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        List<Map<String, Object>> translations =
                (List<Map<String, Object>>) data.get("translations");
        return (String) translations.get(0).get("translatedText");
    }
}
