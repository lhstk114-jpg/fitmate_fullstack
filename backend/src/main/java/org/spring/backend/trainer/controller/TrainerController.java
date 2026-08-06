package org.spring.backend.trainer.controller;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.trainer.dto.TrainerDto;
import org.spring.backend.trainer.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainer")
public class TrainerController {

  private final TrainerService trainerService;
  private final MemberRepository memberRepository;

  // 공통 회원 조회
  private MemberEntity getAuthenticatedMember(CustomUserDetails user) {

    if (user == null) {
      throw new IllegalArgumentException("인증 정보가 없습니다.");
    }

    return memberRepository.findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
  }

  // 트레이너 프로필 조회
  @GetMapping("/my")
  public TrainerDto getMyTrainer(
      @AuthenticationPrincipal CustomUserDetails user) {

    MemberEntity member = getAuthenticatedMember(user);

    return trainerService.getTrainerByMemberId(member.getId());
  }

  // 트레이너 프로필 수정
  @PutMapping("/my")
  public TrainerDto updateMyTrainer(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody TrainerDto trainerDto) {

    MemberEntity member = getAuthenticatedMember(user);

    return trainerService.updateTrainer(member.getId(), trainerDto);
  }

  // 트레이너 목록
  @GetMapping("/list")
  public ResponseEntity<List<TrainerDto>> getTrainerList() {
    return ResponseEntity.ok(trainerService.getAllTrainers());
  }
}