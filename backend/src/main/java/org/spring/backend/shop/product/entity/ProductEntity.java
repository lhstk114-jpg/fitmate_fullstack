package org.spring.backend.shop.product.entity;

import java.util.ArrayList;
import java.util.List;

import org.spring.backend.common.BasicTime;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.product.type.BillingType;
import org.spring.backend.shop.product.type.ProductStatus;
import org.spring.backend.shop.product.type.ProductType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
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
@Table(name = "product_tb")
public class ProductEntity extends BasicTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Long id;

  @Column(nullable = false)
  private String productName;

  @Column(nullable = false)
  private int price;

  @Column(nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductType productType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BillingType billingType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductStatus productStatus;

  @Column(nullable = false)
  private String category;

  @Column
  private int duration; // 이용기간(일)

  @Column
  private int sessionCount; // PT 횟수

  // //N:1
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private MemberEntity memberEntity;

  // 파일엔티티와 1:N 매핑
  @JsonIgnore
  @OneToMany(mappedBy = "productEntity", 
  cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FileEntity> fileEntities = new ArrayList<>();

}
