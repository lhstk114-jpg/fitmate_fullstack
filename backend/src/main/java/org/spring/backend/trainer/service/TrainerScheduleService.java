package org.spring.backend.trainer.service;

import java.util.List;

import org.spring.backend.trainer.dto.TrainerScheduleDto;

public interface TrainerScheduleService {

  // 트레이너 스케줄 등록
  Long insertSchedule(
      TrainerScheduleDto trainerScheduleDto,
      Long trainerId);

  // 트레이너 스케줄 조회
  List<TrainerScheduleDto> getTrainerSchedule(
      Long trainerId);

  // 스케줄 삭제
  void deleteSchedule(
      Long scheduleId,
      Long trainerId);

  // 스케줄 상태 변경
  void updateStatus(
      Long scheduleId,
      Long trainerId);

}