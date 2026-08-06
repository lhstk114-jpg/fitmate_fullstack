package org.spring.backend.shop.MemberProduct.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.shop.MemberProduct.entity.MemberProductEntity;
import org.spring.backend.shop.product.entity.ProductEntity;

import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProductDto {

  private Long id;

  // 상품 정보
  private Long productId;

  private String productName;

  private String productType;

  private String productImage;

  // 이용 기간
  private LocalDateTime startDate;

  private LocalDateTime endDate;

  // PT 횟수
  private Integer totalCount;

  private Integer remainingCount;

  // 상태
  private String status;

  private LocalDateTime createTime;


  public static MemberProductDto toMemberProductDto(MemberProductEntity memberProductEntity) {

    ProductEntity product = memberProductEntity.getProductEntity();

    return MemberProductDto.builder()
        .id(memberProductEntity.getId())
        .productId(product.getId())
        .productName(product.getProductName())
        .productType(
            product.getProductType().name())
        .productImage(
            Optional.ofNullable(product.getFileEntities())
                .orElse(Collections.emptyList())
                .stream()
                .findFirst()
                .map(FileEntity::getNewFileName)
                .orElse(null))
        .startDate(memberProductEntity.getStartDate())
        .endDate(memberProductEntity.getEndDate())
        .totalCount(memberProductEntity.getTotalCount())
        .remainingCount(memberProductEntity.getRemainingCount())
        .status(memberProductEntity.getStatus())
        .createTime(memberProductEntity.getCreateTime())
        .build();
  }
}
