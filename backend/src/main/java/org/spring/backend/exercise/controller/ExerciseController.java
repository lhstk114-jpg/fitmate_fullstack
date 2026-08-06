package org.spring.backend.exercise.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.backend.exception.RateLimitExceededException;
import org.spring.backend.exercise.dto.ExerciseDetail;
import org.spring.backend.exercise.dto.ExerciseDto;
import org.spring.backend.exercise.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 운동 루틴 생성/추천/히스토리/이미지 API 컨트롤러
 * 프론트: RoutineForm.jsx, RoutineResult.jsx, HistoryList.jsx, RoutinePage.jsx가 이 컨트롤러를 호출
 * ※ 필드 타입(ExerciseService, ExerciseSyncService 등)은 모두 인터페이스이며,
 *   각각의 Impl 클래스(ExerciseServiceImpl 등)가 유일한 구현체라 Spring이 자동으로 주입함
 */
@Slf4j
@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final ExerciseSyncService syncService;
    private final RateLimiterService rateLimiterService;
    private final ValidValuesService validValuesService;
    private final ExerciseTranslationService translationService;
    private final ExerciseImageService exerciseImageService;

    /** 프론트엔드 드롭다운 구성용 - 유효한 target/equip 목록 (RoutineForm.jsx 최초 로딩 시 호출) */
    @GetMapping("/options")
    public ResponseEntity<?> getOptions() {
        return ResponseEntity.ok(Map.of(
                "targets", validValuesService.getTargetMap(),
                "equips", validValuesService.getEquipMap()
        ));
    }

    // 부위에 해당하는 장비만 선택 (RoutineForm.jsx에서 타겟 부위 선택 시 장비 드롭다운을 다시 채움)
    @GetMapping("/options/equipments")
    public ResponseEntity<Map<String, String>> getEquipmentsByTarget(@RequestParam String target) {
        Map<String, String> equipMap = exerciseService.getEquipmentsMapByTarget(target);
        return ResponseEntity.ok(equipMap);
    }

    /**
     * 루틴 생성 요청
     * 1) 레이트리밋 확인(사용자당 1분 5회) → 초과 시 429 계열 예외 발생
     * 2) 부위/장비 조건으로 루틴 생성 후 응답
     */
    @PostMapping("/recommend")
    public ResponseEntity<?> getRecommendation(@RequestBody ExerciseDto.Request request, Authentication auth) {
        if (!rateLimiterService.isAllowed(auth.getName())) {
            throw new RateLimitExceededException("루틴 생성 요청이 너무 잦습니다. 1분 후 다시 시도해주세요.");
        }

        ExerciseService.RoutineResult result = exerciseService.generateRoutine(
                auth.getName(), request.getTarget(), request.getEquip());
        return ResponseEntity.ok(ExerciseDto.Response.from(result.plan(), result.details()));
    }

    /** 로그인한 사용자의 과거 루틴 생성 히스토리를 최신순으로 반환 (페이지네이션, 최근 5개까지만 존재) */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        List<ExerciseDto.Response> history = exerciseService.getHistory(auth.getName(), page, size)
                .stream()
                .map(ExerciseDto.Response::from)
                .toList();
        return ResponseEntity.ok(history);
    }

    /**
     * 메인 페이지 "오늘의 추천 운동"용.
     * 로그인한 사용자의 최근 루틴 3개에서 등장한 운동을 제외하고,
     * 캐싱된 운동 중 5개를 무작위로 반환한다. 저장 없음, RapidAPI 호출 없음
     * (전부 로컬 DB 기반이라 쿼터 걱정 없이 자주 호출해도 된다).
     */
    @GetMapping("/quick-pick/personalized")
    public ResponseEntity<?> personalizedQuickPick(Authentication auth) {
        List<ExerciseDetail> picks = exerciseService.personalizedQuickPick(auth.getName());
        return ResponseEntity.ok(picks);
    }

    /**
     * 특정 부위를 동기화하고, 이어서 번역까지 함께 처리한다 (관리자 전용, @PreAuthorize로 ADMIN 권한 강제).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync/{target}")
    public ResponseEntity<?> syncTarget(@PathVariable String target, Authentication auth) {
        syncService.syncByTarget(target);
        int translatedCount = translationService.translateMissingNames();

        return ResponseEntity.ok(Map.of(
                "syncedTarget", target,
                "translatedCount", translatedCount
        ));
    }

    /**
     * 유효한 모든 target을 순차 동기화하고, 전체 완료 후 번역까지 한 번에 처리한다 (관리자 전용).
     * ✅ 수정: 기존에는 이 컨트롤러가 syncService.syncAllTargets(List)와 동일한 반복문+sleep 로직을
     *    직접 다시 구현하고 있었음 (성공/실패 기록, 호출 간 0.5초 대기 등이 완전히 중복).
     *    ExerciseSyncService.syncAllTargets(List<String>)를 그대로 호출하도록 바꿔 중복을 제거함.
     *    동작 방식은 동일 (target별 성공 여부를 Map으로 반환, 호출 간 0.5초 대기).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync/all")
    public ResponseEntity<?> syncAllTargets() {
        Map<String, Boolean> syncResult = syncService.syncAllTargets(validValuesService.getAllValidTargets());

        int translatedCount = translationService.translateMissingNames();

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("syncResult", syncResult);
        response.put("translatedCount", translatedCount);
        return ResponseEntity.ok(response);
    }

    /**
     * name_ko 등이 비어있는 운동들만 수동으로 번역하고 싶을 때 (관리자 전용).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/translate")
    public ResponseEntity<?> translateMissingNames() {
        int count = translationService.translateMissingNames();
        return ResponseEntity.ok(Map.of("translatedCount", count));
    }

    /**
     * 특정 운동의 GIF 이미지를 캐시 우선으로 전달한다.
     * 최초 요청 시에만 RapidAPI를 호출하고, 이후엔 디스크 캐시에서 서빙한다
     * (RapidAPI 크레딧 절약을 위한 필수 장치).
     * 인증 불필요 - <img> 태그가 직접 GET 하므로 커스텀 헤더를 못 붙임.
     */
    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_GIF_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String id) throws IOException {
        byte[] gifBytes = exerciseImageService.getImage(id, "360");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .body(gifBytes);
    }
}
