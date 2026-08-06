
package org.spring.backend.trainer.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.spring.backend.shop.reservation.entity.ReservationEntity;
import org.spring.backend.shop.reservation.repository.ReservationRepository;
import org.spring.backend.shop.reservation.type.ReservationStatus;
import org.spring.backend.shop.reservation.type.ScheduleStatus;
import org.spring.backend.trainer.dto.TrainerScheduleDto;
import org.spring.backend.trainer.entity.TrainerEntity;
import org.spring.backend.trainer.entity.TrainerScheduleEntity;
import org.spring.backend.trainer.repository.TrainerRepository;
import org.spring.backend.trainer.repository.TrainerScheduleRepository;
import org.spring.backend.trainer.service.TrainerScheduleService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TrainerScheduleServiceImpl
                implements TrainerScheduleService {

        private final TrainerScheduleRepository trainerScheduleRepository;
        private final TrainerRepository trainerRepository;
        private final ReservationRepository reservationRepository;

        // 스케줄 등록
        @Override
        @Transactional
        public Long insertSchedule(
                        TrainerScheduleDto dto,
                        Long trainerId) {

                TrainerEntity trainer = trainerRepository.findById(trainerId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "트레이너가 없습니다."));

                TrainerScheduleEntity schedule = TrainerScheduleEntity.builder()
                                .trainer(trainer)
                                .title(dto.getTitle())
                                .content(dto.getContent())
                                .startTime(dto.getStartTime())
                                .endTime(dto.getEndTime())
                                .status(ScheduleStatus.AVAILABLE)
                                .build();

                trainerScheduleRepository.save(schedule);

                return schedule.getId();
        }

        // 트레이너 스케줄 조회
        @Override
        public List<TrainerScheduleDto> getTrainerSchedule(Long memberId) {

                TrainerEntity trainer = trainerRepository.findByMemberId(memberId)
                                .orElseThrow(() -> new IllegalArgumentException("트레이너 정보가 없습니다."));

                return trainerScheduleRepository
                                .findByTrainerId(trainer.getId())
                                .stream()
                                .map(TrainerScheduleDto::toDto)
                                .toList();
        }

        // 스케줄 삭제
        @Override
        @Transactional
        public void deleteSchedule(
                        Long scheduleId,
                        Long trainerId) {

                TrainerScheduleEntity schedule = trainerScheduleRepository.findById(scheduleId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "스케줄이 없습니다."));

                // 본인 스케줄인지 확인
                if (!schedule.getTrainer()
                                .getId()
                                .equals(trainerId)) {

                        throw new IllegalArgumentException(
                                        "본인 스케줄만 삭제 가능합니다.");
                }

                // 예약된 시간은 삭제 방지
                if (schedule.getStatus() == ScheduleStatus.RESERVED) {

                        throw new IllegalArgumentException(
                                        "예약된 시간은 삭제할 수 없습니다.");
                }

                trainerScheduleRepository.delete(schedule);

        }

        // 예약 상태 변경
        @Override
        @Transactional
        public void updateStatus(
                        Long scheduleId,
                        Long trainerId) {

                TrainerScheduleEntity schedule = trainerScheduleRepository.findById(scheduleId)
                                .orElseThrow();

                if (!schedule.getTrainer()
                                .getId()
                                .equals(trainerId)) {

                        throw new IllegalArgumentException(
                                        "담당 트레이너만 변경 가능합니다.");
                }

                if (schedule.getStatus() == ScheduleStatus.AVAILABLE) {

                        schedule.setStatus(
                                        ScheduleStatus.BLOCKED);

                } else {

                        schedule.setStatus(
                                        ScheduleStatus.AVAILABLE);
                }

        }

}