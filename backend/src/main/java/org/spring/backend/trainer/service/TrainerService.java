package org.spring.backend.trainer.service;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.trainer.dto.TrainerDto;

public interface TrainerService {

  public TrainerDto getTrainerByMemberId(Long memberId);

  public TrainerDto createTrainer(Long memberId, TrainerDto trainerDto);

  // 전체 트레이너 조회
  List<TrainerDto> getAllTrainers();

  //어드민 페이지 role 변경으로 트레이너 tb 생성
  void createTrainerByRoleChange(MemberEntity member);

  public TrainerDto updateTrainer(Long id, TrainerDto trainerDto);

}
