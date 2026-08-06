package org.spring.backend.shop.cart.service.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.cart.dto.CartDto;
import org.spring.backend.shop.cart.dto.CartListDto;
import org.spring.backend.shop.cart.entity.CartEntity;
import org.spring.backend.shop.cart.entity.CartListEntity;
import org.spring.backend.shop.cart.repository.CartListRepository;
import org.spring.backend.shop.cart.repository.CartRepository;
import org.spring.backend.shop.cart.service.CartService;
import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.order.entity.OrderItemEntity;
import org.spring.backend.shop.order.repository.OrderRepository;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;
  private final CartListRepository cartListRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;

  @Override
  public void insertCart(String userEmail, CartListDto cartListDto) {

    MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    ProductEntity productEntity = productRepository.findById(cartListDto.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

    // 회원의 장바구니 조회
    CartEntity cartEntity = cartRepository
        .findByMemberEntity_UserEmail(userEmail)
        .orElse(null);

    // 장바구니가 없으면 생성
    if (cartEntity == null) {
      cartEntity = CartEntity.builder()
          .memberEntity(memberEntity)
          .build();

      cartEntity = cartRepository.save(cartEntity);
    }

    // 장바구니 상품 생성, 같은 상품은 수량 증가
    Optional<CartListEntity> existingCartItem = cartListRepository.findByCartEntityIdAndProductEntityId(
        cartEntity.getId(),
        productEntity.getId());

    if (existingCartItem.isPresent()) {

      CartListEntity cartListEntity = existingCartItem.get();

      cartListEntity.setQuantity(
          cartListEntity.getQuantity()
              + cartListDto.getQuantity());

    } else {

      CartListEntity cartListEntity = CartListEntity.builder()
          .cartEntity(cartEntity)
          .productEntity(productEntity)
          .quantity(cartListDto.getQuantity())
          .build();

      cartListRepository.save(cartListEntity);
    }

  }

  @Override
  @Transactional(readOnly = true)
  public List<CartListDto> cartList(String userEmail) {
    CartEntity cartEntity = cartRepository.findByMemberEntity_UserEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

    return cartEntity.getCartListEntities()
        .stream()
        .map(CartListDto::toCartListDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public int countCartItems(String userEmail) {
    return cartListRepository.countByCartEntity_MemberEntity_UserEmail(userEmail);
  }

  @Override
  public void updateQuantity(Long cartItemId, CartListDto cartListDto) {
    CartListEntity cartListEntity = cartListRepository.findById(cartItemId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

    cartListEntity.setQuantity(cartListDto.getQuantity());
  }

  @Override
  public void deleteCartItem(Long cartItemId) {
    cartListRepository.deleteById(cartItemId);
  }

  @Override
  public void clearCart(String userEmail) {
    cartListRepository.deleteByCartEntity_MemberEntity_UserEmail(userEmail);
  }

  @Override
  public void deletePurchasedItems(Long orderId) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

    Optional<CartEntity> cartOptional = cartRepository.findByMemberEntity_UserEmail(
            order.getMemberEntity().getUserEmail()
    );
    if (cartOptional.isEmpty()) {
      return;
    }
    CartEntity cart = cartOptional.get();

    for (OrderItemEntity orderItem : order.getOrderItemEntities()) {

      cartListRepository
          .findByCartEntityIdAndProductEntityId(
              cart.getId(),
              orderItem.getProductEntity().getId())
          .ifPresent(cartListRepository::delete);
    }
  }

}
