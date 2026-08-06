package org.spring.backend.shop.subscription.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.type.ProductType;
import org.spring.backend.shop.subscription.entity.SubscriptionEntity;
import org.spring.backend.shop.subscription.type.SubscriptionStatus;

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
public class SubscriptionDto {
        private Long id;

        private SubscriptionStatus subscriptionStatus;

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        private LocalDateTime nextPaymentDate;

        private LocalDateTime createTime;

        private Long productId;

        private String productName;

        private String productImage;

        private ProductType productType;

        private String paymentMethod;

        private Integer totalCount;

        private Integer remainingCount;

        public static SubscriptionDto toSubscriptionDto(SubscriptionEntity subscriptionEntity) {
                return SubscriptionDto.builder()
                                .id(subscriptionEntity.getId())
                                .subscriptionStatus(subscriptionEntity.getSubscriptionStatus())
                                .startDate(subscriptionEntity.getStartDate())
                                .endDate(subscriptionEntity.getEndDate())
                                .nextPaymentDate(subscriptionEntity.getNextPaymentDate())
                                .createTime(subscriptionEntity.getCreateTime())
                                .productId(subscriptionEntity.getProductEntity().getId())
                                .productName(subscriptionEntity.getProductEntity().getProductName())
                                .productImage(
                                                Optional.ofNullable(subscriptionEntity.getProductEntity())
                                                                .map(ProductEntity::getFileEntities)
                                                                .orElse(Collections.emptyList())
                                                                .stream()
                                                                .findFirst()
                                                                .map(FileEntity::getNewFileName)
                                                                .orElse(null))
                                .productType(
                                                subscriptionEntity
                                                                .getProductEntity()
                                                                .getProductType())
                                .paymentMethod(
                                                Optional.ofNullable(subscriptionEntity.getPaymentEntities())
                                                                .orElse(Collections.emptyList())
                                                                .stream()
                                                                .findFirst()
                                                                .map(payment -> payment.getPaymentMethod().name())
                                                                .orElse(null))
                                .totalCount(
                                                subscriptionEntity.getMemberProductEntity() != null
                                                                ? subscriptionEntity.getMemberProductEntity()
                                                                                .getTotalCount()
                                                                : null)

                                .remainingCount(
                                                subscriptionEntity.getMemberProductEntity() != null
                                                                ? subscriptionEntity.getMemberProductEntity()
                                                                                .getRemainingCount()
                                                                : null)
                                .build();
        }
}
