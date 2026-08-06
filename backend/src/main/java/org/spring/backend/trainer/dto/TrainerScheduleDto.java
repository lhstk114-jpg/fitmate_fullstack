package org.spring.backend.trainer.dto;

import java.time.LocalDateTime;

import org.spring.backend.shop.reservation.type.ScheduleStatus;
import org.spring.backend.trainer.entity.TrainerScheduleEntity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TrainerScheduleDto {

  private Long id;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String title;

  private String content;

  private ScheduleStatus status;

  private Long trainerId;

  private String trainerName;

  private String memberName;

  private boolean editable;

  public static TrainerScheduleDto toDto(TrainerScheduleEntity entity) {

    return TrainerScheduleDto.builder()
        .id(entity.getId())
        .startTime(entity.getStartTime())
        .endTime(entity.getEndTime())
        .title(entity.getTitle())
        .content(entity.getContent())
        .status(entity.getStatus())
        .trainerId(entity.getTrainer().getId())
        .trainerName(
            entity.getTrainer()
                .getMember()
                .getUserName())
        .editable(true) // 트레이너가 만든 일정은 기본 수정 가능
        .build();
  }

}