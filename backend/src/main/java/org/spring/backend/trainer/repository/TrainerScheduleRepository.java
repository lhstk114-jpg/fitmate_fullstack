package org.spring.backend.trainer.repository;

import java.util.List;
import java.util.Optional;

import org.spring.backend.shop.reservation.type.ScheduleStatus;
import org.spring.backend.trainer.entity.TrainerScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerScheduleRepository
                extends JpaRepository<TrainerScheduleEntity, Long> {

        // 특정 트레이너의 전체 스케줄 조회
        List<TrainerScheduleEntity> findByTrainerId(Long trainerId);

        // 특정 트레이너의 예약 가능한 시간 조회
        List<TrainerScheduleEntity> findByTrainerIdAndStatus(
                        Long trainerId,
                        ScheduleStatus status);

        // 특정 날짜의 트레이너 스케줄 조회
        List<TrainerScheduleEntity> findByTrainerIdAndStartTimeBetween(
                        Long trainerId,
                        java.time.LocalDateTime start,
                        java.time.LocalDateTime end);

        Optional<TrainerScheduleEntity> findByReservationId(Long reservationId);

}