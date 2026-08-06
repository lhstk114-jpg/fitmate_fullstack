package org.spring.backend.shop.reservation.repository;

import java.time.LocalDate;
import java.util.List;

import org.spring.backend.shop.reservation.entity.ReservationEntity;
import org.spring.backend.shop.reservation.type.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
  @Query("""
      select r
      from ReservationEntity r
      join fetch r.member
      join fetch r.trainer t
      join fetch t.member
      """)
  List<ReservationEntity> findAllWithMemberAndTrainer();

  List<ReservationEntity> findByTrainerIdOrderByReservationDateDescReservationTimeDesc(Long trainerId);
  List<ReservationEntity> findByMemberIdOrderByReservationDateDescReservationTimeDesc(Long memberId);

  List<ReservationEntity>
  findByTrainer_IdAndReservationDateAndReservationStatus(
      Long trainerId,
      LocalDate reservationDate,
      ReservationStatus reservationStatus);

  @Query("SELECT r FROM ReservationEntity r " +
          "WHERE r.trainer.member.id = :trainerId " +
          "AND r.reservationDate = (" +
          "    SELECT MIN(r2.reservationDate) " +
          "    FROM ReservationEntity r2 " +
          "    WHERE r2.trainer.member.id = :trainerId " +
          "    AND r2.member.id = r.member.id" +
          ")")
  Page<ReservationEntity> findByTrainerId(@Param("trainerId") Long trainerId, Pageable pageable);

  @Query("SELECT r FROM ReservationEntity r WHERE r.trainer.member.id = :trainerId AND r.member.userName LIKE %:search%")
  Page<ReservationEntity> findByTrainerIdAndUserName(
          @Param("trainerId") Long trainerId,
          @Param("search") String search,
          Pageable pageable);
}
