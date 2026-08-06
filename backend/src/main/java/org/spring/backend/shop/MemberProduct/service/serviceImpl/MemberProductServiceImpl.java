package org.spring.backend.shop.MemberProduct.service.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.MemberProduct.dto.MemberProductDto;
import org.spring.backend.shop.MemberProduct.entity.MemberProductEntity;
import org.spring.backend.shop.MemberProduct.repository.MemberProductRepository;
import org.spring.backend.shop.MemberProduct.service.MemberProductService;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.type.ProductType;
import org.spring.backend.shop.subscription.repository.SubscriptionRepository;
import org.spring.backend.shop.subscription.type.SubscriptionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProductServiceImpl implements MemberProductService {

  private final MemberProductRepository memberProductRepository;
  private final MemberRepository memberRepository;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  @Transactional
  public void create(MemberEntity memberEntity, ProductEntity productEntity, LocalDate startDate) {
    // 시작일 설정 (전달받은 startDate가 없으면 오늘 날짜)
    LocalDateTime startDateTime = (startDate != null)
        ? startDate.atStartOfDay()
        : LocalDateTime.now();

    MemberProductEntity memberProductEntity = MemberProductEntity.builder()
        .memberEntity(memberEntity)
        .productEntity(productEntity)
        .startDate(startDateTime) // 💡 버그 수정: LocalDateTime.now() 대신 계산된 startDateTime 적용
        .endDate(startDateTime.plusDays(productEntity.getDuration()))
        .totalCount(productEntity.getSessionCount())
        .remainingCount(productEntity.getSessionCount())
        .status("ACTIVE")
        .build();

    memberProductRepository.save(memberProductEntity);
  }

  @Override
  public List<MemberProductDto> getActivePtProducts(String email) {

    MemberEntity member = memberRepository.findByUserEmail(email)
        .orElseThrow(() -> new RuntimeException("회원 없음"));

    // 이제 MemberEntity 파라미터가 정상적으로 매핑됩니다.
    return memberProductRepository
        .findActivePtProducts(member)
        .stream()
        .map(MemberProductDto::toMemberProductDto)
        .toList();
  }

  public boolean checkSubscribe(String email) {

    MemberEntity member = memberRepository.findByUserEmail(email)
        .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

    return subscriptionRepository
            .existsByMemberEntityAndProductEntity_ProductTypeAndSubscriptionStatus(
                    member,
                    ProductType.PREMIUM,
                    SubscriptionStatus.ACTIVE
            );
            
  }
  public List<MemberProductDto> getMyProducts(Long memberId) {

    return memberProductRepository
            .findByMemberEntityId(memberId)
            .stream()
            .map(MemberProductDto::toMemberProductDto)
            .toList();
}
}