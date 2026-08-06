package org.spring.backend.shop.reservation.service;

import java.util.List;

import org.spring.backend.shop.reservation.dto.ReservationDto;
import org.spring.backend.shop.reservation.type.ReservationStatus;

public interface ReservationService {
        // 예약 생성
        Long reservationInsert(ReservationDto reservationDto);

        // 회원 ID 기반 예약 목록 조회
        List<ReservationDto> getMemberReservation(Long memberId);

        // 트레이너용 예약 조회
        public List<ReservationDto> getTrainerReservation(Long trainerId);

        // 회원 예약 시간 변경
        void updateReservation(
                        Long reservationId,
                        ReservationDto reservationDto,
                        Long memberId);

        // 회원 예약 취소
        void cancelMemberReservation(
                        Long reservationId,
                        Long memberId);

        // 트레이너 예약 취소
        void cancelTrainerReservation(
                        Long reservationId,
                        Long trainerId);

        // 트레이너 예약 상태 변경
        void updateStatus(
                        Long reservationId,
                        ReservationStatus status,
                        Long trainerId);

        List<String> getReservedTimes(Long trainerId, String date);
}
