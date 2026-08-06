package org.spring.backend.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMemberDto {

    // 현재 구독 유지 중인 회원 수
    private Long activeSubscriptionCount;

    // 일정 기간 내 구독 만료 예정 회원 수
    private Long expiringSubscriptionCount;

    // 구독이 종료된 회원 수
    private Long expiredSubscriptionCount;

    // 미구독 회원 수
    private Long unsubscribedMemberCount;

    // 구독률
    private Double subscriptionRate;

    // 관심사 등록률
    private Double interestRegistrationRate;
}