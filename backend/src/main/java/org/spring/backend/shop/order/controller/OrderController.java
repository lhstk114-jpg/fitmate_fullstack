package org.spring.backend.shop.order.controller;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.order.dto.CartOrderRequestDto;
import org.spring.backend.shop.order.dto.OrderDto;
import org.spring.backend.shop.order.dto.SubscriptionOrderRequestDto;
import org.spring.backend.shop.order.service.OrderService;
import org.spring.backend.shop.order.type.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {
  private final OrderService orderService;
  private final MemberRepository memberRepository;

  // 상품 상세에서 바로 주문
  @PostMapping("/direct")
  public ResponseEntity<Long> directOrder(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody OrderDto orderDto) {
    MemberEntity member = memberRepository
        .findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
    Long orderId = orderService.insertDirectOrder(
        member.getId(),
        orderDto);
    return ResponseEntity.ok(orderId);
  }

  // 장바구니 주문
  @PostMapping("/cart")
  public ResponseEntity<Long> cartOrder(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody CartOrderRequestDto request) {
    MemberEntity member = memberRepository
        .findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

    Long orderId = orderService.insertCartOrder(
        member.getId(),
        request.getCartIds(),
        request.getOrder());

    return ResponseEntity.ok(orderId);
  }

  // 주문 목록
  @GetMapping("/list")
  public ResponseEntity<List<OrderDto>> orderList(
      @AuthenticationPrincipal CustomUserDetails user) {
    MemberEntity member = memberRepository
        .findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
    List<OrderDto> orders = orderService.orderList(member.getId());

    return ResponseEntity.ok(orders);
  }

  // 관리자 주문 전체 조회
  @GetMapping("/orderList")
  public ResponseEntity<Page<OrderDto>> orderList(
      @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {

    return ResponseEntity.ok(
        orderService.adminOrderList(pageable));
  }

  // 주문 상세
  @GetMapping("/detail/{orderId}")
  public ResponseEntity<OrderDto> orderDetail(
      @PathVariable Long orderId) {

    return ResponseEntity.ok(orderService.orderDetail(orderId));
  }

  // 주문 취소
  @DeleteMapping("/{orderId}")
  public ResponseEntity<Void> cancelOrder(
      @PathVariable Long orderId) {

    orderService.cancelOrder(orderId);
    return ResponseEntity.ok().build();
  }

  // 배송 상태 변경
  @PutMapping("/{orderId}/delivery-status")
  public ResponseEntity<Void> updateStatus(
      @PathVariable Long orderId,
      @RequestParam DeliveryStatus deliveryStatus) {

    orderService.updateOrderStatus(orderId, deliveryStatus);
    return ResponseEntity.ok().build();
  }

  // PT / GYM / PREMIUM 주문
  @PostMapping("/subscription")
  public ResponseEntity<Long> subscriptionOrder(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody SubscriptionOrderRequestDto request) {

    MemberEntity member = memberRepository
        .findByUserEmail(user.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

    Long orderId = orderService.insertSubscriptionOrder(
        member.getId(),
        request);

    return ResponseEntity.ok(orderId);
  }

}
