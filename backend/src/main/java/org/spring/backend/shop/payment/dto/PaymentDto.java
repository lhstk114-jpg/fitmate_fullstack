package org.spring.backend.shop.payment.dto;

import java.time.LocalDateTime;

import org.spring.backend.shop.payment.entity.PaymentEntity;
import org.spring.backend.shop.payment.type.PaymentMethod;
import org.spring.backend.shop.payment.type.PaymentStatus;

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
public class PaymentDto {
  private Long id;

  // 카카오페이 결제 ID
  private String tid;

  // 토큰
  private String pgToken;

  // 결제수단, 업체
  private PaymentMethod paymentMethod;

  // 결제금액
  private int amount;

  // 결제 승인 시간
  private LocalDateTime approveTime;

  private PaymentStatus paymentStatus;

  private String paymentReadyJson;

  private String paymentApproveJson;

  // 주문 ID
  private Long orderId;

  private Long subscriptionId;

  private String productName;

  private LocalDateTime createTime;

  public static PaymentDto toPaymentDto(PaymentEntity paymentEntity) {
    return PaymentDto.builder()
        .id(paymentEntity.getId())
        .tid(paymentEntity.getTid())
        .pgToken(paymentEntity.getPgToken())
        .paymentMethod(paymentEntity.getPaymentMethod())
        .amount(paymentEntity.getAmount())
        .approveTime(paymentEntity.getApproveTime())
        .paymentStatus(paymentEntity.getPaymentStatus())
        .paymentReadyJson(paymentEntity.getPaymentReadyJson())
        .paymentApproveJson(paymentEntity.getPaymentApproveJson())
        .orderId(paymentEntity.getOrderEntity() != null
            ? paymentEntity.getOrderEntity().getId()
            : null)
        .subscriptionId(paymentEntity.getSubscriptionEntity() != null
            ? paymentEntity.getSubscriptionEntity().getId()
            : null)
        .productName(
            paymentEntity.getOrderEntity() != null
                ? paymentEntity.getOrderEntity()
                    .getOrderItemEntities()
                    .get(0)
                    .getProductName()
                : paymentEntity.getSubscriptionEntity() != null
                    ? paymentEntity.getSubscriptionEntity()
                        .getProductEntity()
                        .getProductName()
                    : null)
        .createTime(paymentEntity.getCreateTime())
        .build();
  }

}
