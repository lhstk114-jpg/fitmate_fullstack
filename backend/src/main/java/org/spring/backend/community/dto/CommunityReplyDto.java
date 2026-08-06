package org.spring.backend.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.backend.community.entity.CommunityReplyEntity;

import java.time.LocalDateTime;

/**
 * 댓글 응답/요청 DTO
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommunityReplyDto {
  private Long id;

  private String emoticon;

  private String content;

  private String userName;

  private String userEmail;

  private Long communityId;

  private Long memberId;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  /**
   * 엔티티 → DTO 변환
   * - 댓글 작성자명(userName)/이메일(userEmail)/memberId는 댓글 엔티티에 직접 저장된 값을 우선 사용하고,
   *   없으면 연관된 회원(memberEntity)에서 가져옴 (탈퇴 등으로 회원 정보가 사라진 경우를 대비한 폴백)
   * - 둘 다 없으면 "탈퇴한 사용자입니다"로 표시
   */
  public static CommunityReplyDto toReplyDto(CommunityReplyEntity replyEntity) {
    String userName = "탈퇴한 사용자입니다";

    if (replyEntity.getUserName() != null && !replyEntity.getUserName().isEmpty()) {
      userName = replyEntity.getUserName();
    } else if (replyEntity.getMemberEntity() != null && replyEntity.getMemberEntity().getUserName() != null) {
      userName = replyEntity.getMemberEntity().getUserName();
    }

    String userEmail = null;
    if (replyEntity.getUserEmail()!=null){
      userEmail = replyEntity.getUserEmail();
    } else if (replyEntity.getMemberEntity()!=null) {
      userEmail = replyEntity.getMemberEntity().getUserEmail();
    }

    Long memberId = null;
    if (replyEntity.getMemberId() != null){
      memberId = replyEntity.getMemberId();
    } else if (replyEntity.getMemberEntity()!=null) {
      memberId = replyEntity.getMemberEntity().getId();
    }

    return CommunityReplyDto.builder()
            .id(replyEntity.getId())
            .content(replyEntity.getContent())
            .userName(userName) // 수정된 로직 적용
            .userEmail(userEmail)
            .communityId(replyEntity.getCommunityId())
            .memberId(memberId)
            .createTime(replyEntity.getCreateTime())
            .updateTime(replyEntity.getUpdateTime())
            .build();
  }
}
