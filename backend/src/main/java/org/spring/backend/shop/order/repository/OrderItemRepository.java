package org.spring.backend.shop.order.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.order.entity.OrderItemEntity;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

        @Query("""
                SELECT oi.productEntity From OrderItemEntity oi
                GROUP BY oi.productEntity
                ORDER BY SUM(oi.quantity) DESC
                """)
        List<ProductEntity> findPopularProducts(Pageable pageable);
        // 1. order_item_tb에서 주문된 상품들을 가져옴
        // 2. product별로 묶음
        // 3. quantity 합산, 많이 팔린 순으로 정렬
        // 4. Pageable로 상위 5개만 가져옴

        @Query("""
                SELECT oi.productEntity FROM OrderItemEntity oi
                WHERE oi.productEntity.category = :category
                GROUP BY oi.productEntity
                ORDER BY SUM(oi.quantity) DESC
                """)
        List<ProductEntity> findPopularProductsByCategory(
                        @Param("category") String category,
                        Pageable pageable);
        // 1. order_item_tb에서 주문된 상품들을 가져옴
        // 2. 그중 ProductEntity.category가 회원 관심사와 매칭된 category인 것만 필터링
        // 3. product별로 묶음
        // 4. quantity 합산, 많이 팔린 순으로 정렬
        // 5. Pageable로 상위 5개만 가져옴

        // 대시보드용: productEntity와 누적 판매량 포함
        @Query("""
                SELECT oi.productEntity, SUM(oi.quantity)
                FROM OrderItemEntity oi
                GROUP BY oi.productEntity
                ORDER BY SUM(oi.quantity) DESC
                """)
        List<Object[]> findPopularProductsWithSalesCount(Pageable pageable);
        // 위와 동일


        //시작일 -> 종료일 까지의 매출 합계
        @Query("""
                SELECT COALESCE(SUM(oi.price * oi.quantity), 0)
                FROM OrderItemEntity oi
                WHERE oi.createTime >= :startDate
                AND oi.createTime < :endDate
                """)
        Long sumSalesBetween(
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );
        // 1. 시작일과 종료일을 service에서 파라미터로 받아옴
        // 2. order_item_tb에서 시작일과 종료일 사이에 주문된 상품들을 가져옴
        // 3. 주문된 상품의 price*quantity 로 금액 계산
        // 4. 각 항목들의 금액 합산으로 기간 내의 매출 합계 계산
}
