package org.spring.backend.shop.subscription.service;

import java.util.List;

import org.spring.backend.shop.subscription.dto.SubscriptionDto;

public interface SubscriptionService {
      // 구독 생성 (구독 시작)
    void insertSubscription(Long memberId, Long productId, SubscriptionDto subscriptionDto);

    public void insertPremiumSubscription(Long memberId);

    // 내 구독 목록
    List<SubscriptionDto> subscriptionList(Long memberId);

    // 구독 상세
    SubscriptionDto subscriptionDetail(Long memberId,Long subscriptionId);

    // 구독 상태 변경 (ACTIVE / CANCEL / PAUSED 등)
    void updateSubscriptionStatus(Long memberId, Long subscriptionId, SubscriptionDto subscriptionDto);

    // 구독 취소
    void cancelSubscription(Long memberId, Long subscriptionId);

    // 다음 결제일 갱신 (자동결제용)
    void updateNextPaymentDate(Long subscriptionId);

    // 프리미엄 여부
    public boolean isPremium(Long memberId) ;
}
