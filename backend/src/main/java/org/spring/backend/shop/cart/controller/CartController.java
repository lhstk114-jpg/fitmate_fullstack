package org.spring.backend.shop.cart.controller;

import java.util.List;

import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.shop.cart.dto.CartDto;
import org.spring.backend.shop.cart.dto.CartListDto;
import org.spring.backend.shop.cart.service.CartService;
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
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartService;

  // 장바구니 추가
  @PostMapping
  public ResponseEntity<Void> addCart(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody CartListDto cartListDto) {

    cartService.insertCart(
        user.getUsername(),
        cartListDto);

    return ResponseEntity.ok().build();
  }

  // 내 장바구니 조회
  @GetMapping
  public ResponseEntity<List<CartListDto>> cartList(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        cartService.cartList(userDetails.getUsername()));
  }

  // 수량 수정
  @PutMapping("/{cartItemId}")
  public ResponseEntity<Void> updateQuantity(
      @PathVariable("cartItemId") Long cartItemId,
      @RequestBody CartListDto cartListDto) {

    cartService.updateQuantity(cartItemId, cartListDto);
    return ResponseEntity.ok().build();
  }

  // 장바구니 삭제 (단건)
  @DeleteMapping("/{cartItemId}")
  public ResponseEntity<Void> deleteCart(@PathVariable("cartItemId") Long cartItemId) {
    cartService.deleteCartItem(cartItemId);
    return ResponseEntity.ok().build();
  }

  // 장바구니 전체 삭제
  @DeleteMapping("/clear")
  public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
    cartService.clearCart(
        userDetails.getUsername());
    return ResponseEntity.ok().build();
  }
}
