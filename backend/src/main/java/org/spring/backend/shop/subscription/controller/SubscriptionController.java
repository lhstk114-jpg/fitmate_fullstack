package org.spring.backend.shop.subscription.controller;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.subscription.dto.SubscriptionDto;
import org.spring.backend.shop.subscription.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription")
public class SubscriptionController {

        private final SubscriptionService subscriptionService;
        private final MemberRepository memberRepository;

        // 회원 조회 공통 메서드
        private MemberEntity getMember(CustomUserDetails user) {

                return memberRepository
                                .findByUserEmail(user.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
        }

        // 구독 신청
        @PostMapping("/{productId}")
        public ResponseEntity<Void> insertSubscription(
                        @AuthenticationPrincipal CustomUserDetails user,
                        @PathVariable Long productId,
                        @RequestBody SubscriptionDto subscriptionDto) {

                MemberEntity member = getMember(user);

                subscriptionService.insertSubscription(
                                member.getId(),
                                productId,
                                subscriptionDto);

                return ResponseEntity.ok().build();
        }

        // 내 구독 목록
        @GetMapping("/my")
        public ResponseEntity<List<SubscriptionDto>> subscriptionList(
                        @AuthenticationPrincipal CustomUserDetails user) {

                MemberEntity member = getMember(user);

                return ResponseEntity.ok(
                                subscriptionService.subscriptionList(member.getId()));
        }

        // 구독 상세
        @GetMapping("/detail/{subscriptionId}")
        public ResponseEntity<SubscriptionDto> subscriptionDetail(
                        @AuthenticationPrincipal CustomUserDetails user,
                        @PathVariable Long subscriptionId) {

                MemberEntity member = getMember(user);

                return ResponseEntity.ok(
                                subscriptionService.subscriptionDetail(
                                                member.getId(),
                                                subscriptionId));
        }

        // 구독 상태 변경
        @PutMapping("/{subscriptionId}/status")
        public ResponseEntity<Void> updateSubscriptionStatus(
                        @AuthenticationPrincipal CustomUserDetails user,
                        @PathVariable Long subscriptionId,
                        @RequestBody SubscriptionDto subscriptionDto) {

                MemberEntity member = getMember(user);

                subscriptionService.updateSubscriptionStatus(
                                member.getId(),
                                subscriptionId,
                                subscriptionDto);

                return ResponseEntity.ok().build();
        }

        // 구독 취소
        @PutMapping("/{subscriptionId}/cancel")
        public ResponseEntity<Void> cancelSubscription(
                        @AuthenticationPrincipal CustomUserDetails user,
                        @PathVariable Long subscriptionId) {

                MemberEntity member = getMember(user);

                subscriptionService.cancelSubscription(
                                member.getId(),
                                subscriptionId);

                return ResponseEntity.ok().build();
        }

        // 다음 결제일 갱신
        @PutMapping("/{subscriptionId}/nextPayment")
        public ResponseEntity<Void> updateNextPaymentDate(
                        @PathVariable Long subscriptionId) {

                subscriptionService.updateNextPaymentDate(subscriptionId);

                return ResponseEntity.ok().build();
        }

        // 프리미엄 구독
        @PostMapping("/premium")
        public ResponseEntity<Void> insertPremiumSubscription(
                        @AuthenticationPrincipal CustomUserDetails user) {

                MemberEntity member = getMember(user);

                subscriptionService.insertPremiumSubscription(
                                member.getId());

                return ResponseEntity.ok().build();
        }

        @GetMapping("/premium/check")
        public ResponseEntity<Boolean> checkPremium(
                        @AuthenticationPrincipal CustomUserDetails user) {

                MemberEntity member = getMember(user);

                return ResponseEntity.ok(
                                subscriptionService.isPremium(member.getId()));
        }
}