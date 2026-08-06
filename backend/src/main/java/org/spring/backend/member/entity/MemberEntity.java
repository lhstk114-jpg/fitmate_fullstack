package org.spring.backend.member.entity;

import jakarta.persistence.*;
import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.common.BasicTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.entity.CommunityReplyEntity;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.member.dto.MemberDto;
import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.subscription.entity.SubscriptionEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "member")
public class MemberEntity extends BasicTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(unique = true, nullable = false)
  private String userEmail;

  @Column(nullable = false)
  private String userPw;

  @Column(nullable = false)
  private String userName;

  private String userAddress;

  private String userPhone;

  private int subscribe;

  @Column(nullable = false)
  private int profilePhoto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  // 멤버의 추가데이터와 1:1매칭
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "member_add_id")
  private MemberAddEntity memberAddEntity;

  // 파일엔티티와 1:N 매핑
  @OneToMany(mappedBy = "memberEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<FileEntity> fileEntities;

  //게시판엔티티와 1:N 매핑
  @OneToMany(mappedBy = "memberEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<CommunityEntity> communityEntities;
  //게시판댓글엔티티와 1:N 매핑
  @OneToMany(mappedBy = "memberEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<CommunityReplyEntity> communityReplyEntities;

  // 구독 상품 매핑
  @OneToMany(mappedBy = "memberEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE,
          orphanRemoval = true)
  @JsonIgnore
  private List<SubscriptionEntity> subscriptionEntities;

  @OneToMany(mappedBy = "memberEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
  @JsonIgnore
  private List<OrderEntity> orderEntities;

  @OneToMany(mappedBy = "memberEntity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
  @JsonIgnore
  private List<PersonalScheduleEntity> personalScheduleEntities;

  public static MemberEntity toInsertMemberEntity(MemberDto memberDto, String encodePw) {
    return MemberEntity.builder()
        .userEmail(memberDto.getUserEmail())
        .userPw(encodePw)
        .userName(memberDto.getUserName())
        .userAddress(memberDto.getUserAddress())
        .userPhone(memberDto.getUserPhone())
        .subscribe(0)
        .profilePhoto(0)
        .role(Role.MEMBER)
        .build();
  }
  public static MemberEntity toInsertMemberAdminEntity(MemberDto memberDto, String encodePw) {
    return MemberEntity.builder()
        .userEmail(memberDto.getUserEmail())
        .userPw(encodePw)
        .userName(memberDto.getUserName())
        .userAddress(memberDto.getUserAddress())
        .userPhone(memberDto.getUserPhone())
        .subscribe(0)
        .profilePhoto(0)
        .role(memberDto.getRole())
        .build();
  }
}
