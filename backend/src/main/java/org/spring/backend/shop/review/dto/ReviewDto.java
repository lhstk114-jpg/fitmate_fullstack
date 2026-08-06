package org.spring.backend.shop.review.dto;

import java.time.LocalDateTime;

import org.spring.backend.shop.review.entity.ReviewEntity;

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
public class ReviewDto {
  private Long id;
  private int rating;

  private String content;

  private Long memberId;

  private Long orderItemId;

  private String memberName;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  public static ReviewDto toReviewDto(ReviewEntity reviewEntity) {
    return ReviewDto.builder()
        .id(reviewEntity.getId())
        .rating(reviewEntity.getRating())
        .content(reviewEntity.getContent())
        .memberId(reviewEntity.getMemberEntity() != null
            ? reviewEntity.getMemberEntity().getId()
            : null)
        .orderItemId(reviewEntity.getOrderItemEntity() != null
            ? reviewEntity.getOrderItemEntity().getId()
            : null)
        .memberName(reviewEntity.getMemberEntity() != null
            ? reviewEntity.getMemberEntity().getUserName()
            : null)
        .createTime(reviewEntity.getCreateTime())
        .updateTime(reviewEntity.getUpdateTime())
        .build();

  }
}
