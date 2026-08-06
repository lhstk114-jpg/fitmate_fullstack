package org.spring.backend.shop.cart.repository;

import java.util.Optional;

import org.spring.backend.shop.cart.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartEntity,Long>{

  Optional<CartEntity> findByMemberEntity_UserEmail(String userEmail);

  
}
