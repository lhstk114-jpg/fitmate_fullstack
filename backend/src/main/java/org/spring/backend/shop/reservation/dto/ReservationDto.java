package org.spring.backend.shop.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.spring.backend.shop.reservation.entity.ReservationEntity;
import org.spring.backend.shop.reservation.type.ReservationStatus;

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
public class ReservationDto {

  private Long id;

  private LocalDate reservationDate;

  private LocalTime reservationTime;

  private ReservationStatus reservationStatus;

  private String memo;

  private Long memberId;

  private String memberName;

  private Long trainerId;

  private String trainerName;

  private Long memberProductId;

  private Integer lessonNumber;
  private Integer totalCount;

  public static ReservationDto toReservationDto(
      ReservationEntity entity) {

    return ReservationDto.builder()
        .id(entity.getId())
        .reservationDate(entity.getReservationDate())
        .reservationTime(entity.getReservationTime())
        .reservationStatus(entity.getReservationStatus())
        .memo(entity.getMemo())
        .memberId(entity.getMember().getId())
        .memberName(entity.getMember().getUserName())
        .trainerId(entity.getTrainer().getId())
        .trainerName(entity.getTrainer().getMember().getUserName())
        .memberProductId(entity.getMemberProduct().getId())
        .lessonNumber(entity.getLessonNumber())
        .totalCount(entity.getMemberProduct().getTotalCount())
        .build();
  }
}
