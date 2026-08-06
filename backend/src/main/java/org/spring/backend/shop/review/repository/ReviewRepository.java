package org.spring.backend.shop.review.repository;

import java.util.List;

import org.spring.backend.shop.review.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
  List<ReviewEntity> findByProductEntity_Id(Long productId);

  List<ReviewEntity> findByMemberEntity_Id(Long memberId);

  boolean existsByOrderItemEntity_Id(Long orderItemId);
}
