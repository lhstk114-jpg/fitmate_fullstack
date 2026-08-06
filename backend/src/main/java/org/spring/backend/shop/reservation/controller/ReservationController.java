package org.spring.backend.shop.reservation.controller;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.reservation.dto.ReservationDto;
import org.spring.backend.shop.reservation.service.ReservationService;
import org.spring.backend.shop.reservation.type.ReservationStatus;
import org.spring.backend.trainer.entity.TrainerEntity;
import org.spring.backend.trainer.repository.TrainerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservationService reservationService;
  private final MemberRepository memberRepository;
  private final TrainerRepository trainerRepository;

  // 공통 회원 조회
  private MemberEntity getAuthenticatedMember(CustomUserDetails user) {

    if (user == null) {
      throw new IllegalArgumentException("인증 정보가 없습니다.");
    }

    return memberRepository.findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
  }

  // 회원 예약 생성
  @PostMapping
  public ResponseEntity<Long> reservationInsert(
      @RequestBody ReservationDto reservationDto,
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getAuthenticatedMember(user);

    reservationDto.setMemberId(member.getId());

    Long id = reservationService.reservationInsert(reservationDto);

    return ResponseEntity.ok(id);
  }

  // 로그인한 회원 예약 목록
  @GetMapping("/my")
  public ResponseEntity<List<ReservationDto>> getMyReservations(
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getAuthenticatedMember(user);

    return ResponseEntity.ok(
        reservationService.getMemberReservation(member.getId()));
  }

  // 특정 회원 예약 목록 (관리자용)
  @GetMapping("/member/{memberId}")
  public ResponseEntity<List<ReservationDto>> getMemberReservation(
      @PathVariable Long memberId) {

    return ResponseEntity.ok(
        reservationService.getMemberReservation(memberId));
  }

  // 트레이너 본인 예약 조회
  @GetMapping("/trainer")
  public ResponseEntity<List<ReservationDto>> getTrainerReservation(
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getAuthenticatedMember(user);

    TrainerEntity trainer = trainerRepository.findByMemberId(member.getId())
        .orElseThrow();

    return ResponseEntity.ok(
        reservationService.getTrainerReservation(trainer.getId()));
  }

  // 회원 예약 시간 변경
  @PutMapping("/{reservationId}")
  public ResponseEntity<Void> updateReservation(
      @PathVariable Long reservationId,
      @RequestBody ReservationDto reservationDto,
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getAuthenticatedMember(user);

    reservationService.updateReservation(
        reservationId,
        reservationDto,
        member.getId());

    return ResponseEntity.ok().build();
  }

  // 회원 예약 취소
  @PutMapping("/{reservationId}/member-cancel")
  public ResponseEntity<Void> cancelMemberReservation(
      @PathVariable Long reservationId,
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getAuthenticatedMember(user);

    reservationService.cancelMemberReservation(
        reservationId,
        member.getId());

    return ResponseEntity.ok().build();
  }

  // 트레이너 예약 상태 변경
  @PutMapping("/{reservationId}/status")
  public ResponseEntity<Void> updateStatus(
      @PathVariable Long reservationId,
      @RequestParam ReservationStatus status,
      @RequestParam Long trainerId) {

    reservationService.updateStatus(
        reservationId,
        status,
        trainerId);

    return ResponseEntity.ok().build();
  }

  // 트레이너 예약 취소
  @PutMapping("/{reservationId}/trainer-cancel")
  public ResponseEntity<Void> cancelTrainerReservation(
      @PathVariable Long reservationId,
      @RequestParam Long trainerId) {

    reservationService.cancelTrainerReservation(
        reservationId,
        trainerId);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/reserved-times")
  public ResponseEntity<List<String>> getReservedTimes(
      @RequestParam Long trainerId,
      @RequestParam String date) {

    return ResponseEntity.ok(
        reservationService.getReservedTimes(trainerId, date));
  }
}