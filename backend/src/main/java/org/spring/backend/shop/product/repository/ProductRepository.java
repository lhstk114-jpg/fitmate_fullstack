package org.spring.backend.shop.product.repository;

import java.util.List;
import java.util.Optional;

import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.type.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  @EntityGraph(attributePaths = "fileEntities")
  Page<ProductEntity> findByProductType(ProductType productType, Pageable pageable);

  @EntityGraph(attributePaths = "fileEntities")
  Page<ProductEntity> findByProductNameContaining(String keyword, Pageable pageable);

  @EntityGraph(attributePaths = "fileEntities")
  Optional<ProductEntity> findById(Long id);

  @EntityGraph(attributePaths = "fileEntities")
  Page<ProductEntity> findAll(Pageable pageable);

  boolean existsByProductName(String productName);

  @EntityGraph(attributePaths = "fileEntities")
  Optional<ProductEntity> findFirstByProductType(ProductType productType);

  boolean existsByProductType(ProductType productType);

  // 주문 수량(quantity) 합계 기준 판매량 TOP N 조회
  @Query("""
          SELECT oi.productEntity
          FROM OrderItemEntity oi
          GROUP BY oi.productEntity
          ORDER BY SUM(oi.quantity) DESC
      """)
  List<ProductEntity> findTopSalesProducts(Pageable pageable);

  List<ProductEntity> findAllByOrderByIdDesc(Pageable fallbackPageable);
}
