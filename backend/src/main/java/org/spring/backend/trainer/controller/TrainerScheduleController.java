package org.spring.backend.trainer.controller;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.reservation.dto.ReservationDto;
import org.spring.backend.shop.reservation.service.ReservationService;
import org.spring.backend.trainer.dto.TrainerScheduleDto;
import org.spring.backend.trainer.entity.TrainerEntity;
import org.spring.backend.trainer.repository.TrainerRepository;
import org.spring.backend.trainer.service.TrainerScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainer/schedule")
public class TrainerScheduleController {

  private final TrainerScheduleService trainerScheduleService;
  private final TrainerRepository trainerRepository;
  private final ReservationService reservationService;
  private final MemberRepository memberRepository;

  // 회원 조회 공통 메서드
  private MemberEntity getMember(CustomUserDetails user) {

    return memberRepository
        .findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
  }

  // 트레이너 스케줄 등록
  @PostMapping
  public ResponseEntity<Long> insertSchedule(
      @RequestParam Long trainerId,
      @RequestBody TrainerScheduleDto dto) {

    Long id = trainerScheduleService.insertSchedule(
        dto,
        trainerId);

    return ResponseEntity.ok(id);
  }

  // 트레이너 스케줄 조회
  @GetMapping
  public ResponseEntity<List<TrainerScheduleDto>> getSchedule(
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getMember(user);

    List<TrainerScheduleDto> list = trainerScheduleService.getTrainerSchedule(member.getId());

    return ResponseEntity.ok(list);
  }

  @GetMapping("/reservation")
  public ResponseEntity<List<ReservationDto>> getTrainerReservation(
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getMember(user);

    TrainerEntity trainer = trainerRepository
        .findByMemberId(member.getId())
        .orElseThrow();

    return ResponseEntity.ok(
        reservationService.getTrainerReservation(trainer.getId()));
  }

  // 스케줄 삭제
  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<Void> deleteSchedule(
      @PathVariable Long scheduleId,
      @RequestParam Long trainerId) {

    trainerScheduleService.deleteSchedule(
        scheduleId,
        trainerId);

    return ResponseEntity.ok().build();
  }

  // 스케줄 상태 변경 (가능 ↔ 휴무)
  @PutMapping("/{scheduleId}/status")
  public ResponseEntity<Void> updateStatus(
      @PathVariable Long scheduleId,
      @RequestParam Long trainerId) {

    trainerScheduleService.updateStatus(
        scheduleId,
        trainerId);

    return ResponseEntity.ok().build();
  }

}