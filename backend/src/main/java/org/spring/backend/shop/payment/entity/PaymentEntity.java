package org.spring.backend.shop.payment.entity;

import java.time.LocalDateTime;

import org.spring.backend.common.BasicTime;
import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.payment.type.PaymentMethod;
import org.spring.backend.shop.payment.type.PaymentStatus;
import org.spring.backend.shop.subscription.entity.SubscriptionEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "payment_tb")
public class PaymentEntity extends BasicTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Long id;

  private String tid; // 카카오결제ID

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod paymentMethod;// 결제업체, 수단

  @Column(nullable = false)
  private int amount; // 결제금액

  @Column
  private String pgToken; // 토큰

  @Column
  private LocalDateTime approveTime; // 결제 승인 시간

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus paymentStatus;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String paymentReadyJson; // 카카오페이 ready API 응답 저장

  @Lob
  @Column(columnDefinition = "TEXT")
  private String paymentApproveJson; // 카카오페이 approve API 응답 저장

  // N:1
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  private SubscriptionEntity subscriptionEntity;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderEntity orderEntity;
}
