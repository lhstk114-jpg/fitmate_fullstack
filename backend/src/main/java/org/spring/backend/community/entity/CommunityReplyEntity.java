package org.spring.backend.community.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.spring.backend.common.BasicTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.backend.member.entity.MemberEntity;

/**
 * 댓글 엔티티 (community_reply_tb)
 * - BasicTime을 상속받아 createTime/updateTime 자동 관리 (BasicTime에 @CreatedDate 등이 있는 것으로 추정)
 * - userName/userEmail을 회원 엔티티와 별도로 컬럼에 직접 저장 (회원 탈퇴 후에도 댓글 작성자 정보를 남기기 위한 스냅샷 용도로 보임)
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "community_reply_tb")
public class CommunityReplyEntity extends BasicTime {
  @Id
  @Column(name = "community_reply_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성 당시의 작성자명 스냅샷 (회원 정보가 바뀌거나 탈퇴해도 댓글에는 원래 이름이 남도록)
    private String userName;

    // 작성 당시의 작성자 이메일 스냅샷
    private String userEmail;

    private String content;

    // 이모티콘 종류 (숫자 코드로 관리하는 것으로 추정, 실제 매핑 정의는 프론트/상수 확인 필요)
    private int emoticon;

    // community_id 컬럼의 읽기 전용 사본 (연관관계(communityEntity)와 별개로 값만 바로 꺼내 쓰기 위함)
    @Column(name = "community_id", insertable = false, updatable = false)
    private Long communityId;

    // member_id 컬럼의 읽기 전용 사본 (연관관계(memberEntity)와 별개로 값만 바로 꺼내 쓰기 위함)
    @Column(name = "member_id", insertable = false, updatable = false)
    private Long memberId;

    // 이 댓글이 달린 게시글 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="community_id")
    private CommunityEntity communityEntity;

  // 작성자 회원 정보 (JSON 직렬화 시 순환참조/민감정보 노출 방지를 위해 응답에서 제외)
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private MemberEntity memberEntity;
}
