package org.spring.backend.exercise.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.exercise.dto.ExerciseDetail;
import org.spring.backend.exercise.entity.Exercise;
import org.spring.backend.exercise.entity.ExercisePlan;
import org.spring.backend.exercise.exception.ExerciseNotFoundException;
import org.spring.backend.exercise.exception.InvalidExerciseRequestException;
import org.spring.backend.exercise.repository.ExercisePlanRepository;
import org.spring.backend.exercise.repository.ExerciseRepository;
import org.spring.backend.exercise.service.ExerciseService;
import org.spring.backend.exercise.service.ExerciseSyncService;
import org.spring.backend.exercise.service.ExerciseTranslationService;
import org.spring.backend.exercise.service.ValidValuesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ExerciseService 구현체
 * - 부위별 규칙 기반(AI 미사용)으로 세트/렙/휴식을 부여해 루틴을 생성
 * - 히스토리는 사용자당 최근 5개만 유지 (초과분은 실제로 삭제)
 * - 메인페이지 개인화 추천은 최근 루틴 3개에 등장한 운동을 제외하고 무작위 5개 선택
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExercisePlanRepository planRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseSyncService syncService;
    private final ValidValuesService validValuesService;
    private final ExerciseTranslationService translationService;
    private final ObjectMapper objectMapper;

    // 캐시에 없는 부위를 요청받았을 때 그 자리에서 RapidAPI를 호출할지 여부.
    // 기본값 false: 지금은 DB에 이미 캐싱된 것만 사용하고, RapidAPI는 호출하지 않는다.
    // application.properties에 exercisedb.auto-sync-on-demand=true 를 넣으면 다시 켤 수 있다.
    // 꺼져 있는 동안 데이터를 채우고 싶으면 관리자용 POST /sync/{target}, /sync/all을 수동으로 호출하면 된다.
    @Value("${exercisedb.auto-sync-on-demand:false}")
    private boolean autoSyncOnDemand;

    private static final int MAX_EXERCISES_PER_ROUTINE = 5;
    private static final int RECENT_PLAN_COUNT_FOR_EXCLUSION = 3; // 메인 추천에서 제외할 최근 루틴 개수
    private static final int PERSONALIZED_PICK_COUNT = 5;         // 메인 추천 운동 개수
    private static final int MAX_HISTORY_PER_USER = 5;            // 사용자당 보관할 최근 루틴 개수

    // 부위에 해당하는 장비만 추출 (예: "back" 부위에서 실제로 쓰인 장비들만 { 영문: 한글 } 형태로)
    @Override
    public Map<String, String> getEquipmentsMapByTarget(String target) {
        List<Exercise> exercises = exerciseRepository.findByTargetIgnoreCase(target);
        Map<String, String> equipMap = new LinkedHashMap<>();

        for (Exercise ex : exercises) {
            String eqEn = ex.getEquipment();
            if (eqEn != null && !eqEn.isBlank()) {
                String eqKo = (ex.getEquipKo() != null && !ex.getEquipKo().isBlank()) ? ex.getEquipKo() : eqEn;
                equipMap.put(eqEn.toLowerCase(), eqKo);
            }
        }
        return equipMap;
    }

    /** 부위별 기본 처방(세트/렙/휴식) - 규칙 기반, AI 미사용 */
    private static final Map<String, Prescription> PRESCRIPTION_BY_BODY_PART = Map.ofEntries(
            Map.entry("cardio", new Prescription(1, "15-20분", 0)),
            Map.entry("back", new Prescription(4, "6-10", 90)),
            Map.entry("chest", new Prescription(4, "6-10", 90)),
            Map.entry("upper legs", new Prescription(4, "8-12", 90)),
            Map.entry("shoulders", new Prescription(3, "10-12", 60)),
            Map.entry("upper arms", new Prescription(3, "10-15", 60)),
            Map.entry("waist", new Prescription(3, "15-20", 45))
    );
    private static final Prescription DEFAULT_PRESCRIPTION = new Prescription(3, "8-12", 60);

    /**
     * 루틴 생성 (RoutineForm.jsx → POST /api/exercise/recommend)
     * 0) 입력값 검증 → 1) 캐시 없으면 조건부 동기화 → 2) 부위(+장비) 조건으로 후보 조회
     * → 3) 이름 중복 제거 + 셔플로 다양성 확보 → 4) 부위별 규칙 적용 → 5) 히스토리 저장 + 개수 제한 적용
     */
    @Override
    public ExerciseService.RoutineResult generateRoutine(String userEmail, String muscle, String equipment) {
        // 0. 입력값 검증 - ExerciseDB가 지원하지 않는 값이면 API/DB 조회 전에 바로 차단
        if (muscle == null || muscle.isBlank() || !validValuesService.isValidTarget(muscle)) {
            throw new InvalidExerciseRequestException(
                    "'" + muscle + "'는 지원하지 않는 muscle(target) 값입니다.");
        }
        boolean equipmentSpecified = equipment != null && !equipment.isBlank();
        if (equipmentSpecified && !validValuesService.isValidEquipment(equipment)) {
            throw new InvalidExerciseRequestException(
                    "'" + equipment + "'는 지원하지 않는 equipment 값입니다.");
        }

        // 1. 로컬 캐시에 없으면, 설정이 켜져 있을 때만 그 자리에서 동기화 (지금은 기본 꺼짐)
        if (!syncService.isCached(muscle)) {
            if (autoSyncOnDemand) {
                syncService.syncByTarget(muscle);
                translationService.translateMissingNames();
            } else {
                log.info("자동 동기화 비활성화 상태 - DB 캐시만 사용합니다: target={}", muscle);
            }
        }

        // 2. equipment가 지정된 경우만 부위+장비로 조회, 없으면 부위 전체 조회
        List<Exercise> candidates = equipmentSpecified
                ? exerciseRepository.findByTargetIgnoreCaseAndEquipmentIgnoreCase(muscle, equipment)
                : List.of();
        if (candidates.isEmpty()) {
            candidates = exerciseRepository.findByTargetIgnoreCase(muscle);
        }
        if (candidates.isEmpty()) {
            throw new ExerciseNotFoundException(
                    "'" + muscle + "' 부위에 해당하는 운동을 찾지 못했습니다.");
        }

        // 3. 이름 중복 제거 + 셔플로 매번 다른 조합이 나오도록 다양성 확보
        List<Exercise> deduped = new ArrayList<>(
                candidates.stream()
                        .collect(LinkedHashMap<String, Exercise>::new,
                                (map, ex) -> map.putIfAbsent(ex.getName(), ex),
                                LinkedHashMap::putAll)
                        .values()
        );
        Collections.shuffle(deduped);
        List<Exercise> selected = deduped.subList(0, Math.min(MAX_EXERCISES_PER_ROUTINE, deduped.size()));

        // 4. 부위별 규칙으로 세트/렙/휴식 부여
        List<ExerciseDetail> details = buildDetails(selected);

        String routineText = buildComplexRoutine(details);

        // 화면 타이틀용 한글 부위명은 선택된 운동의 targetKo에서 가져온다 (없으면 muscle 그대로)
        String nameKo = selected.isEmpty() ? muscle : displayTarget(selected.get(0));

        // 히스토리에서 상세를 복원할 수 있도록 exerciseDetails를 JSON으로 함께 저장
        String detailsJson = serializeDetails(details);

        ExercisePlan plan = planRepository.save(
                new ExercisePlan(userEmail, muscle, nameKo, "", routineText, detailsJson));

        enforceHistoryLimit(userEmail); // 최근 5개 초과분 삭제

        return new ExerciseService.RoutineResult(plan, details);
    }

    /**
     * 사용자당 히스토리를 최근 MAX_HISTORY_PER_USER개로 유지한다.
     * 그보다 오래된 것들은 이 시점에 실제로 DB에서 삭제한다 (단순 화면 표시 제한이 아님).
     */
    private void enforceHistoryLimit(String userEmail) {
        List<ExercisePlan> all = planRepository.findByUserEmailOrderByIdDesc(userEmail);
        if (all.size() > MAX_HISTORY_PER_USER) {
            List<ExercisePlan> overflow = all.subList(MAX_HISTORY_PER_USER, all.size());
            planRepository.deleteAll(overflow);
        }
    }

    /**
     * 메인 페이지 "오늘의 추천 운동"용.
     * 로그인한 사용자의 최근 루틴 3개에 등장했던 운동은 제외하고,
     * 캐싱된 운동 전체 중에서 무작위로 5개를 뽑아준다.
     * ExercisePlan을 새로 저장하지 않고, RapidAPI 호출도 하지 않는다 (전부 로컬 DB 기반).
     */
    @Override
    public List<ExerciseDetail> personalizedQuickPick(String userEmail) {
        List<ExercisePlan> recentPlans = planRepository
                .findByUserEmailOrderByIdDesc(userEmail, PageRequest.of(0, RECENT_PLAN_COUNT_FOR_EXCLUSION))
                .getContent();

        Set<String> recentExerciseIds = recentPlans.stream()
                .flatMap(plan -> deserializeDetails(plan.getExerciseDetailsJson()).stream())
                .map(ExerciseDetail::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Exercise> pool = new ArrayList<>(exerciseRepository.findAll());
        pool.removeIf(ex -> recentExerciseIds.contains(ex.getId()));
        Collections.shuffle(pool);

        List<Exercise> picked = pool.subList(0, Math.min(PERSONALIZED_PICK_COUNT, pool.size()));
        return buildDetails(picked);
    }

    /** Exercise 목록 -> 부위별 규칙으로 세트/렙/휴식이 붙은 ExerciseDetail 목록 변환 (공통 로직 추출) */
    private List<ExerciseDetail> buildDetails(List<Exercise> exercises) {
        List<ExerciseDetail> details = new ArrayList<>();
        for (Exercise ex : exercises) {
            String bodyPartKey = ex.getBodyPart() == null ? "" : ex.getBodyPart().toLowerCase();
            Prescription p = PRESCRIPTION_BY_BODY_PART.getOrDefault(bodyPartKey, DEFAULT_PRESCRIPTION);
            details.add(ExerciseDetail.builder()
                    .id(ex.getId())
                    .name(displayName(ex))
                    .target(displayTarget(ex))
                    .equipment(displayEquipment(ex))
                    .sets(p.sets())
                    .reps(p.reps())
                    .restSeconds(p.restSeconds())
                    .build());
        }
        return details;
    }

    @Override
    public List<ExercisePlan> getHistory(String userEmail, int page, int size) {
        return planRepository
                .findByUserEmailOrderByIdDesc(userEmail, PageRequest.of(page, size))
                .getContent();
    }

    // ExerciseDetail 리스트를 JSON 문자열로 직렬화 (실패해도 예외를 던지지 않고 null 반환 - routine 텍스트는 정상 저장되도록)
    private String serializeDetails(List<ExerciseDetail> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            log.warn("exerciseDetails 직렬화 실패. routine 텍스트만 저장됩니다: {}", e.getMessage());
            return null;
        }
    }

    // JSON 문자열 → ExerciseDetail 리스트로 역직렬화 (실패 시 빈 리스트로 폴백, 예전 데이터 호환)
    private List<ExerciseDetail> deserializeDetails(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ExerciseDetail>>() {});
        } catch (Exception e) {
            log.warn("exerciseDetails 역직렬화 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 한글 번역(nameKo)이 있으면 그걸, 없으면(아직 번역 안 된 신규 운동 등)
     * 영문 name으로 폴백. 화면에 표시되는 모든 이름이 여기를 거치게 함.
     */
    private String displayName(Exercise ex) {
        String nameKo = ex.getNameKo();
        return (nameKo != null && !nameKo.isBlank()) ? nameKo : ex.getName();
    }

    private String displayTarget(Exercise ex) {
        String targetKo = ex.getTargetKo();
        return (targetKo != null && !targetKo.isBlank()) ? targetKo : ex.getTarget();
    }

    private String displayEquipment(Exercise ex) {
        String equipKo = ex.getEquipKo();
        return (equipKo != null && !equipKo.isBlank()) ? equipKo : ex.getEquipment();
    }

    // 사람이 읽는 텍스트 형태의 루틴 설명 생성 (ExercisePlan.routine 컬럼에 저장됨)
    private String buildComplexRoutine(List<ExerciseDetail> exercises) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔥 오늘의 맞춤 루틴 추천\n\n");

        for (int i = 0; i < exercises.size(); i++) {
            ExerciseDetail ex = exercises.get(i);
            sb.append(String.format("%d. %s\n", i + 1, ex.getName().toUpperCase()));
            sb.append("   - 목표: ").append(ex.getTarget()).append("\n");
            sb.append("   - 세트: ").append(ex.getSets())
                    .append(" x ").append(ex.getReps())
                    .append(" (휴식 ").append(ex.getRestSeconds()).append("초)\n\n");
        }
        sb.append("오늘도 득근하세요! 💪");
        return sb.toString();
    }

    // 부위별 세트/렙/휴식 처방을 담는 내부 레코드
    private record Prescription(int sets, String reps, int restSeconds) {}
}
