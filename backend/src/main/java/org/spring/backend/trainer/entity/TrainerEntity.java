package org.spring.backend.trainer.entity;

import org.spring.backend.common.BasicTime;
import org.spring.backend.member.entity.MemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "trainer_tb")
public class TrainerEntity extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_id")
    private Long id;


    // 트레이너 회원 정보
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;


    @Column(length = 50)
    private String career; // 경력


    @Column(length = 100)
    private String specialty; // 전문 분야


    @Column(length = 500)
    private String introduce; // 자기소개


    private String certificate; // 자격증


    private String profileImage; // 프로필 이미지

}