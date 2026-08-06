package org.spring.backend.shop.subscription.repository;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.type.ProductType;
import org.spring.backend.shop.subscription.entity.SubscriptionEntity;
import org.spring.backend.shop.subscription.type.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
  List<SubscriptionEntity> findByMemberEntity_Id(Long memberId);

  boolean existsByMemberEntityAndProductEntityAndSubscriptionStatus(
      MemberEntity memberEntity,
      ProductEntity productEntity,
      SubscriptionStatus subscriptionStatus);

  boolean existsByMemberEntity_IdAndSubscriptionStatus(Long memberId, SubscriptionStatus active);

  boolean existsByMemberEntityAndProductEntity_ProductTypeAndSubscriptionStatus(
      MemberEntity memberEntity,
      ProductType productType,
      SubscriptionStatus subscriptionStatus);

  @Query("""
          select s
          from SubscriptionEntity s
          join fetch s.memberProductEntity
          where s.memberEntity.id = :memberId
      """)
  List<SubscriptionEntity> findMySubscriptions(
      @Param("memberId") Long memberId);
}
