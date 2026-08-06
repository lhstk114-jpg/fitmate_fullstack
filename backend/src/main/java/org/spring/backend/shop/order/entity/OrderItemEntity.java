package org.spring.backend.shop.order.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.spring.backend.common.BasicTime;
import org.spring.backend.shop.product.entity.ProductEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "order_item_tb")
public class OrderItemEntity extends BasicTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "orderItem_id")
  private Long id;

  @Column(nullable = false)
  private int price;

  @Column(nullable = false)
  private int quantity;

  private String productName;

  private String productImage;

  @Column
  private LocalDate startDate;

  // N:1
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private ProductEntity productEntity;

  // N:1
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderEntity orderEntity;
}
