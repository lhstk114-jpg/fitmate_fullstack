package org.spring.backend.shop.subscription.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.MemberProduct.entity.MemberProductEntity;
import org.spring.backend.shop.payment.repository.PaymentRepository;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.spring.backend.shop.product.type.ProductType;
import org.spring.backend.shop.subscription.dto.SubscriptionDto;
import org.spring.backend.shop.subscription.entity.SubscriptionEntity;
import org.spring.backend.shop.subscription.repository.SubscriptionRepository;
import org.spring.backend.shop.subscription.service.SubscriptionService;
import org.spring.backend.shop.subscription.type.SubscriptionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;
  private final PaymentRepository paymentRepository;

  @Override
  public void insertPremiumSubscription(Long memberId) {

    // 회원 조회
    MemberEntity memberEntity = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("회원 없음"));

    // 프리미엄 상품 조회
    ProductEntity premiumProduct = productRepository
        .findFirstByProductType(ProductType.PREMIUM)
        .orElseThrow(() -> new RuntimeException("프리미엄 상품 없음"));  

    // 이미 구독 중인지 확인
    boolean exists = subscriptionRepository
        .existsByMemberEntityAndProductEntityAndSubscriptionStatus(
            memberEntity,
            premiumProduct,
            SubscriptionStatus.ACTIVE);

    if (exists) {
      throw new RuntimeException("이미 프리미엄 구독 중입니다.");
    }

    // 구독 생성
    SubscriptionEntity subscription = SubscriptionEntity.builder()
        .memberEntity(memberEntity)
        .productEntity(premiumProduct)
        .subscriptionStatus(SubscriptionStatus.ACTIVE)
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusMonths(1))
        .nextPaymentDate(LocalDateTime.now().plusMonths(1))
        .build();

    subscriptionRepository.save(subscription);
  }

  @Override
  public void insertSubscription(Long memberId, Long productId, SubscriptionDto subscriptionDto) {
    MemberEntity memberEntity = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    ProductEntity product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

    SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
        .memberEntity(memberEntity)
        .productEntity(product)
        .subscriptionStatus(SubscriptionStatus.ACTIVE)
        .nextPaymentDate(
            subscriptionDto.getNextPaymentDate() != null
                ? subscriptionDto.getNextPaymentDate()
                : LocalDateTime.now().plusMonths(1))
        .build();

    subscriptionRepository.save(subscriptionEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SubscriptionDto> subscriptionList(Long memberId) {
    return subscriptionRepository.findByMemberEntity_Id(memberId)
        .stream()
        .map(SubscriptionDto::toSubscriptionDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public SubscriptionDto subscriptionDetail(Long memberId, Long subscriptionId) {

    SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
        .orElseThrow(() -> new IllegalArgumentException("구독 상품이 존재하지 않습니다."));

    return SubscriptionDto.toSubscriptionDto(subscription);
  }

  @Override
  public void updateSubscriptionStatus(Long memberId, Long subscriptionId, SubscriptionDto subscriptionDto) {

    SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
        .orElseThrow(() -> new IllegalArgumentException("구독 상품이 존재하지 않습니다."));

    subscription.setSubscriptionStatus(subscriptionDto.getSubscriptionStatus());
  }

  @Override
  public void cancelSubscription(Long memberId, Long subscriptionId) {
    SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
        .orElseThrow(() -> new IllegalArgumentException("구독 상품이 존재하지 않습니다."));

    if (!subscription.getMemberEntity().getId().equals(memberId)) {
      throw new IllegalArgumentException("접근 권한이 없습니다.");
    }

    subscription.setSubscriptionStatus(SubscriptionStatus.CANCELED);
    subscription.setNextPaymentDate(null);
    subscription.setEndDate(LocalDateTime.now());
  }

  @Override
  public void updateNextPaymentDate(Long subscriptionId) {

    SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
        .orElseThrow(() -> new IllegalArgumentException("구독 상품이 존재하지 않습니다."));

    // 예: 1달 후 자동 갱신
    subscription.setNextPaymentDate(
        subscription.getNextPaymentDate() != null
            ? subscription.getNextPaymentDate().plusMonths(1)
            : LocalDateTime.now().plusMonths(1));
  }
  @Override
  public boolean isPremium(Long memberId) {

    return subscriptionRepository.existsByMemberEntity_IdAndSubscriptionStatus(
            memberId,
            SubscriptionStatus.ACTIVE);
}
}
