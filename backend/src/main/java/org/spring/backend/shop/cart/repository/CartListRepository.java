package org.spring.backend.shop.cart.repository;

import java.util.List;
import java.util.Optional;

import org.spring.backend.shop.cart.entity.CartListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartListRepository extends JpaRepository<CartListEntity, Long> {

  // 특정 장바구니 상품 조회
  List<CartListEntity> findByCartEntityId(Long cartId);


  // 회원 이메일 기준 상품 개수
  int countByCartEntity_MemberEntity_UserEmail(String userEmail);


  // 회원 장바구니 전체 삭제
  void deleteByCartEntity_MemberEntity_UserEmail(String userEmail);


  // 같은 상품이 장바구니에 있는지 확인
  Optional<CartListEntity> findByCartEntityIdAndProductEntityId(Long cartId, Long productId);

}
