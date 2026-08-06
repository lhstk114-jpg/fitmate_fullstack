package org.spring.backend.trainer.entity;

import java.time.LocalDateTime;

import org.spring.backend.common.BasicTime;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.reservation.entity.ReservationEntity;
import org.spring.backend.shop.reservation.type.ScheduleStatus;

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
@Table(name = "trainer_schedule_tb")
public class TrainerScheduleEntity extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_schedule_id")
    private Long id;

    // 담당 트레이너
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    // 일정 제목
    @Column(length = 100)
    private String title;

    // 일정 내용
    @Column(length = 500)
    private String content;

    // 시작 시간
    @Column(nullable = false)
    private LocalDateTime startTime;

    // 종료 시간
    @Column(nullable = false)
    private LocalDateTime endTime;

    // AVAILABLE, RESERVED, BLOCKED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;

    @OneToOne(mappedBy = "trainerSchedule")
    private ReservationEntity reservation;

}