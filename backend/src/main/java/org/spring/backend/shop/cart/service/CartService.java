package org.spring.backend.shop.cart.service;

import java.util.List;

import org.spring.backend.shop.cart.dto.CartListDto;

public interface CartService {

  void insertCart(String userEmail, CartListDto carListDto);

  List<CartListDto> cartList(String userEmail);

  int countCartItems(String userEmail);

  void updateQuantity(Long cartItemId, CartListDto carListDto);

  void deleteCartItem(Long cartItemId);

  // 장바구니 전체 비우기
  void clearCart(String userEmail);

  // 주문한 상품만 삭제
  void deletePurchasedItems(Long orderId);
}
