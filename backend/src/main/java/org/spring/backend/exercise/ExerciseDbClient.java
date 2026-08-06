package org.spring.backend.exercise;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * RapidAPI ExerciseDB 호출을 한 군데로 모은 공통 클라이언트.
 * baseUrl과 인증 헤더를 여기서 한 번만 설정하고,
 * ExerciseSyncServiceImpl / ValidValuesServiceImpl / ExerciseImageServiceImpl은
 * 이 클라이언트만 주입받아 쓴다.
 */
@Component
public class ExerciseDbClient {

    private final RestClient restClient;
    private final String apiKey;

    public ExerciseDbClient(@Value("${exercisedb.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://exercisedb.p.rapidapi.com")
                .defaultHeader("X-RapidAPI-Key", apiKey)
                .defaultHeader("X-RapidAPI-Host", "exercisedb.p.rapidapi.com")
                .build();
    }

    /** /exercises/target/{target} - 특정 부위의 운동 목록 조회 (동기화용) */
    public JsonNode getExercisesByTarget(String target) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/exercises/target/{target}").build(target))
                .retrieve()
                .body(JsonNode.class);
    }

    /** /exercises/targetList - 유효한 target 전체 목록 (ValidValuesService 검증용) */
    public JsonNode getTargetList() {
        return restClient.get()
                .uri("/exercises/targetList")
                .retrieve()
                .body(JsonNode.class);
    }

    /** /exercises/equipmentList - 유효한 equipment 전체 목록 (ValidValuesService 검증용) */
    public JsonNode getEquipmentList() {
        return restClient.get()
                .uri("/exercises/equipmentList")
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * /image - GIF 바이너리를 직접 스트리밍으로 받아온다.
     * (목록 조회 응답엔 더 이상 gifUrl이 없어서, 화면에 보여주려면
     *  exerciseId로 이 엔드포인트를 따로 호출해야 함)
     *
     * @param exerciseId Exercise 엔티티의 id (예: "0023")
     * @param resolution 해상도 (예: "360", 플랜에 따라 상한 있음)
     * @return GIF 바이트 배열
     */
    public byte[] getImage(String exerciseId, String resolution) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/image")
                        .queryParam("exerciseId", exerciseId)
                        .queryParam("resolution", resolution)
                        .queryParam("rapidapi-key", apiKey)
                        .build())
                .retrieve()
                .body(byte[].class);
    }
}
