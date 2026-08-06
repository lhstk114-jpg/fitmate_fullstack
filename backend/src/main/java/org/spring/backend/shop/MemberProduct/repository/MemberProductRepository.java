package org.spring.backend.shop.MemberProduct.repository;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.MemberProduct.entity.MemberProductEntity;
import org.spring.backend.shop.product.type.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberProductRepository extends JpaRepository<MemberProductEntity, Long> {

  // MemberEntity 객체를 직접 넘겨받아서 처리하도록 수정
  @Query("""
      select mp
      from MemberProductEntity mp
      join fetch mp.productEntity p
      where mp.memberEntity = :member
        and p.productType = 'PT'
        and mp.remainingCount > 0
        and mp.status = 'ACTIVE'
  """)
  List<MemberProductEntity> findActivePtProducts(@Param("member") MemberEntity member);

boolean existsByMemberEntityAndProductEntity_ProductTypeAndStatus(
    MemberEntity memberEntity,
    ProductType productType,
    String status
);
List<MemberProductEntity> findByMemberEntityId(Long memberId);
}