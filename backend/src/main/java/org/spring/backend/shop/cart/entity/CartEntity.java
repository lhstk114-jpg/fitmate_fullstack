package org.spring.backend.shop.cart.entity;

import java.util.List;

import org.spring.backend.common.BasicTime;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.payment.entity.PaymentEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "cart")
public class CartEntity extends BasicTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_id")
  private Long id;

  // 1:1 > 한쪽만 설정
  @OneToOne
  @JoinColumn(name = "member_id")
  private MemberEntity memberEntity;

  // 1:N
  @OneToMany(mappedBy = "cartEntity", 
  fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  List<CartListEntity> cartListEntities;
}
