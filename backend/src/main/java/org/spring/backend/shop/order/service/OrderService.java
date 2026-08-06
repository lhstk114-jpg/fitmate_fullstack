package org.spring.backend.shop.order.service;

import java.util.List;

import org.spring.backend.shop.order.dto.OrderDto;
import org.spring.backend.shop.order.dto.SubscriptionOrderRequestDto;
import org.spring.backend.shop.order.type.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

      // 상품 상세에서 바로 주문
      Long insertDirectOrder(Long memberId, OrderDto orderDto);

      // 장바구니에서 주문
      Long insertCartOrder(Long memberId, List<Long> cartIds, OrderDto orderDto);

      // 주문 목록 조회
      List<OrderDto> orderList(Long memberId);

      // 주문 상세 조회
      OrderDto orderDetail(Long orderId);

      // 주문 취소
      void cancelOrder(Long orderId);

      // 주문 상태 변경
      void updateOrderStatus(Long orderId, DeliveryStatus deliveryStatus);

      // 구독 타입 주문생성
      Long insertSubscriptionOrder(Long memberId, SubscriptionOrderRequestDto request);

      // 관리자 주문목록 전체조회
      Page<OrderDto> adminOrderList(Pageable pageable);
}
