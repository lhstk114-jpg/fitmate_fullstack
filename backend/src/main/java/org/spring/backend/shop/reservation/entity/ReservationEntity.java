package org.spring.backend.shop.reservation.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import org.spring.backend.common.BasicTime;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.MemberProduct.entity.MemberProductEntity;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.reservation.type.ReservationStatus;
import org.spring.backend.trainer.entity.TrainerEntity;
import org.spring.backend.trainer.entity.TrainerScheduleEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "reservation_tb")
public class ReservationEntity extends BasicTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reservation_id")
  private Long id;

  @Column(nullable = false)
  private LocalDate reservationDate;// 예약 날짜

  @Column(nullable = false)
  private LocalTime reservationTime;// 예약 시간

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus reservationStatus; // RESERVED, COMPLETE, CANCEL

  @Column(length = 500)
  private String memo; // 요청사항

  @Column(nullable = false)
  private Integer lessonNumber; //PT 회차

  // 예약한 회원
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity member;

  // 담당 트레이너
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trainer_id", nullable = false)
  private TrainerEntity trainer;

  // PT 상품
  @ManyToOne
  @JoinColumn(name = "product_id")
  private ProductEntity ProductEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_product_id")
  private MemberProductEntity memberProduct;

  @OneToOne
  @JoinColumn(name = "trainer_schedule_id")
  private TrainerScheduleEntity trainerSchedule;
}
