package org.spring.backend.shop.cart.entity;

import org.spring.backend.common.BasicTime;
import org.spring.backend.shop.product.entity.ProductEntity;

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
@Table(name = "cart_list_tb")
public class CartListEntity extends BasicTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cartList_id")
  private Long id;

  @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
  private int quantity; // 수량

  // N:1
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id")
  private CartEntity cartEntity;

  // N:1
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private ProductEntity productEntity;

}
