package org.spring.backend.shop.order.repository;

import java.util.List;
import java.util.Optional;

import org.spring.backend.shop.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

  @EntityGraph(attributePaths = { "orderItemEntities" }) // N+1 대응
  List<OrderEntity> findByMemberEntityId(Long memberId);

  // 결제 내역에서는 굿즈 상품만 조회
  @Query("""
          SELECT DISTINCT o
          FROM OrderEntity o
          JOIN FETCH o.orderItemEntities oi
          JOIN FETCH oi.productEntity p
          WHERE o.memberEntity.id = :memberId
          AND p.productType = 'GOODS'
      """)
  List<OrderEntity> findGoodsOrdersByMemberId(@Param("memberId") Long memberId);

  @Query("""
    SELECT o
    FROM OrderEntity o
    LEFT JOIN FETCH o.paymentEntities p
    WHERE o.id = :orderId
""")
Optional<OrderEntity> findDetailById(@Param("orderId") Long orderId);

}
