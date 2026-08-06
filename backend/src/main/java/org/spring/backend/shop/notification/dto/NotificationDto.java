package org.spring.backend.shop.notification.dto;

import java.time.LocalDateTime;

import org.spring.backend.shop.notification.entity.NotificationEntity;

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
public class NotificationDto {
  private Long id;

  private String title;

  private String content;

  private int isRead;

  private Long subscriptionId;

  private Long memberId;

  private LocalDateTime createTime;

  public static NotificationDto fromEntity(NotificationEntity entity) {
    return NotificationDto.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .content(entity.getContent())
        .isRead(entity.getIsRead())
        .subscriptionId(
            entity.getSubscriptionEntity() != null
                ? entity.getSubscriptionEntity().getId()
                : null)
        .memberId(
            entity.getMemberEntity() != null
                ? entity.getMemberEntity().getId()
                : null)
        .createTime(entity.getCreateTime())
        .build();
  }
}
