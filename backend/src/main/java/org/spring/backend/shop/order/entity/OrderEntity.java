package org.spring.backend.shop.order.entity;

import java.util.ArrayList;
import java.util.List;

import org.spring.backend.common.BasicTime;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.order.type.DeliveryStatus;
import org.spring.backend.shop.order.type.OrderStatus;
import org.spring.backend.shop.payment.entity.PaymentEntity;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "order_tb")
public class OrderEntity extends BasicTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long id;

  @Column(nullable = false)
  private int totalPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus orderStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeliveryStatus deliveryStatus;

  @Column
  private String receiverName;

  @Column
  private String receiverPhone;

  @Column
  private String receiverAddress;

  @Column
  private String receiverDetailAddress;

  @Column
  private String deliveryMemo;

  // //N:1
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private MemberEntity memberEntity;

  @JsonIgnore
  @OneToMany(mappedBy = "orderEntity", fetch = FetchType.LAZY)
  private List<OrderItemEntity> orderItemEntities = new ArrayList<>();

  @OneToMany(mappedBy = "orderEntity", fetch = FetchType.LAZY)
  private List<PaymentEntity> paymentEntities = new ArrayList<>();
}
