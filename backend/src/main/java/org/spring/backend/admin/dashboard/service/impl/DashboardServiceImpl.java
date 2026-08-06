package org.spring.backend.admin.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import org.spring.backend.admin.dashboard.dto.*;
import org.spring.backend.admin.dashboard.service.DashboardService;
import org.spring.backend.member.enumtype.Interest;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.repository.CommunityRepository;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.order.repository.OrderItemRepository;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final CommunityRepository communityRepository;

    @Override
    public DashboardResponseDto getDashboardData() {
        DashboardSummaryDto summary = getSummary();
        DashboardMemberDto member = getMember();

        List<DashboardChartDto> interestChart = getInterestChart();
        List<DashboardChartDto> communityChart = getCommunityChart();
        List<DashboardChartDto> salesChart = getSalesChart();
        List<DashboardChartDto> subscriptionChart = getSubscriptionChart();

        List<DashboardCommunityDto> communityList = getCommunityTop5();
        List<DashboardProductDto> productList = getProductTop5();

        return DashboardResponseDto.builder()
                .summary(summary)
                .member(member)
                .interestChart(interestChart)
                .communityChart(communityChart)
                .salesChart(salesChart)
                .subscriptionChart(subscriptionChart)
                .communityList(communityList)
                .productList(productList)
                .build();
    }

    /* ==================== Summary ==================== */
    // 대시보드 주요 현황 조회
    private DashboardSummaryDto getSummary() {
        // 현재 날짜
        LocalDate today = LocalDate.now();
        // ==================== 날짜 범위 ====================
        // 오늘 시작 (00:00)
        LocalDateTime startOfToday = today.atStartOfDay();
        // 내일 시작 (오늘 데이터 포함)
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        // 이번 달 시작
        LocalDateTime startOfCurrentMonth = today.withDayOfMonth(1).atStartOfDay();
        // 지난달 시작
        LocalDateTime startOfLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        // 지난달 종료 = 이번 달 시작
        LocalDateTime endOfLastMonth = startOfCurrentMonth;

        // ==================== 기본 통계 ====================
        // 총 상품 개수
        Long totalProduct = productRepository.count();
        // 총 회원 수
        Long totalMember = memberRepository.countByRole(Role.MEMBER);
        // 오늘 작성된 글 수
        Long todayCommunityCount = communityRepository
                .countByCreateTimeGreaterThanEqualAndCreateTimeLessThanAndTabNameNot(startOfToday, startOfTomorrow, "공지사항");
        // 오늘 매출
        Long todaySales = orderItemRepository.sumSalesBetween(startOfToday, startOfTomorrow);
        // 지난달 전체 매출
        Long lastMonthSales = orderItemRepository.sumSalesBetween(startOfLastMonth, endOfLastMonth);
        // 이번 달 누적 매출
        Long currentMonthSales = orderItemRepository.sumSalesBetween(startOfCurrentMonth, startOfTomorrow);

        return DashboardSummaryDto.builder()
                .totalMemberCount(totalMember)
                .totalProductCount(totalProduct)
                .todayCommunityCount(todayCommunityCount)
                .todaySales(todaySales)
                .currentMonthSales(currentMonthSales)
                .lastMonthSales(lastMonthSales)
                .build();
    }

    /* ==================== Member(공사중) ==================== */
    // 회원 및 구독 CRM 조회
    private DashboardMemberDto getMember() {
        // 일반 회원 수
        Long totalMember = memberRepository.countByRole(Role.MEMBER);
        // 현재 구독 중인 일반 회원
        Long activeSubs =
                memberRepository.countByRoleAndSubscribe(Role.MEMBER, 1);



        // 구독상품 관련 작업 끝나면 추가
        Long expiringSubs = 0L;
        Long expiredSubs = 0L;



        // 미구독 회원
        Long unSubs =
                memberRepository.countByRoleAndSubscribe(Role.MEMBER, 0);

        // 관심사 등록 회원 수
        Long interestRegistered = 0L;

        Pageable pageable = PageRequest.of(0, 1);

        for (Interest interest : Interest.values()) {
            interestRegistered += memberRepository
                    .findByInterest(pageable, interest.toString())
                    .getTotalElements();
        }

        // 구독률
        Double subscriptionRate =
                totalMember == 0 ? 0.0 : Math.round((activeSubs * 100.0) / totalMember * 10) / 10.0;
        // 관심사 등록률
        Double interestRegistrationRate =
                totalMember == 0 ? 0.0 : Math.round((interestRegistered * 100.0) / totalMember * 10) / 10.0;

        return DashboardMemberDto.builder()
                .activeSubscriptionCount(activeSubs)
                .expiringSubscriptionCount(expiringSubs)
                .expiredSubscriptionCount(expiredSubs)
                .unsubscribedMemberCount(unSubs)
                .subscriptionRate(subscriptionRate)
                .interestRegistrationRate(interestRegistrationRate)
                .build();
    }

    /* ==================== Product ==================== */
    // 상품 판매량 TOP 5 조회
    private List<DashboardProductDto> getProductTop5() {
        Pageable pageable = PageRequest.of(0, 5);

        List<Object[]> result =
                orderItemRepository.findPopularProductsWithSalesCount(pageable);
        return result.stream()
                .map(row -> {
                    ProductEntity product = (ProductEntity) row[0];
                    Long salesCount = ((Number) row[1]).longValue();

                    return DashboardProductDto.builder()
                            .id(product.getId())
                            .productName(product.getProductName())
                            .price(product.getPrice())
                            .salesCount(salesCount)
                            .build();
                })
                .toList();
    }

    /* ==================== Community ==================== */
    // 커뮤니티 게시글 TOP 5 조회
    private List<DashboardCommunityDto> getCommunityTop5() {

        List<CommunityEntity> result =
                communityRepository.findTop5ByTabNameNotOrderByHitDesc("공지사항");
        return result.stream()
                .map(entity -> DashboardCommunityDto.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .categoryName(entity.getCategoryName())
                        .hit(entity.getHit())
                        .build())
                .toList();
    }

    /* ==================== Chart ==================== */
    // 회원 구독 현황 그래프
    private List<DashboardChartDto> getSubscriptionChart(){

        Long activeSubs =
                memberRepository.countByRoleAndSubscribe(Role.MEMBER, 1);
        Long unSubs=
                memberRepository.countByRoleAndSubscribe(Role.MEMBER, 0);

       List<DashboardChartDto> subscriptionChart = new ArrayList<>();

        subscriptionChart.add(
                DashboardChartDto.builder()
                        .label("구독 회원")
                        .value(activeSubs)
                        .build()
        );
        subscriptionChart.add(
                DashboardChartDto.builder()
                        .label("미구독 회원")
                        .value(unSubs)
                        .build()
        );
            return subscriptionChart;
        }


    // 회원 관심사 분포 조회
    private List<DashboardChartDto> getInterestChart() {
        Pageable pageable = PageRequest.of(0, 1);
//        System.out.println("=======관심사==========");
//        System.out.println(memberRepository.findByInterest(pageable,"DIET"));
//        System.out.println(memberRepository.findByInterest(pageable,"WORKOUT"));
//        System.out.println(memberRepository.findByInterest(pageable,"HEALTH"));

        List<DashboardChartDto> interestChart = new ArrayList<>();

        long totalMember = memberRepository.countByRole(Role.MEMBER);

        long totalInterestMember = 0;

        for (Interest interest : Interest.values()) {

            long count = memberRepository
                    .findByInterest(pageable, interest.toString())
                    .getTotalElements();

            totalInterestMember += count;

            interestChart.add(
                    DashboardChartDto.builder()
                            .label(interest.toString())
                            .value(count)
                            .build()
            );
        }
        //관심사가 없는 회원 수
        interestChart.add(
                DashboardChartDto.builder()
                        .label("UNREGISTERED")
                        .value(totalMember - totalInterestMember)
                        .build()
        );

        return interestChart;
    }


    // 게시글 작성 추이 조회
    private List<DashboardChartDto> getCommunityChart() {
        // 현재 날짜
        LocalDate today = LocalDate.now();
        // 오늘을 포함한 최근 7일의 시작 날짜
        LocalDate chartStartDate = today.minusDays(6);

        List<DashboardChartDto> communityChart = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            // 현재 조회할 날짜
            LocalDate date = chartStartDate.plusDays(i);
            // 조회 날짜의 시작 시간
            LocalDateTime startDate = date.atStartOfDay();
            // 다음 날 00:00을 조회 종료 시간으로 사용
            LocalDateTime endDate = date.plusDays(1).atStartOfDay();
            // 해당 날짜에 작성된 게시글 합계 조회
            Long count =
                    communityRepository.countByCreateTimeGreaterThanEqualAndCreateTimeLessThanAndTabNameNot(startDate, endDate, "공지사항");
            // 차트 데이터 생성
            communityChart.add(DashboardChartDto.builder()
                    // 프론트에서 날짜 형식 가공
                    .label(date.toString())
                    .value(count)
                    .build()
            );
        }

        return communityChart;
    }

    // 최근 7일 매출 추이 조회
    private List<DashboardChartDto> getSalesChart() {
        // 현재 날짜
        LocalDate today = LocalDate.now();
        // 오늘을 포함한 최근 7일의 시작 날짜
        LocalDate chartStartDate = today.minusDays(6);

        List<DashboardChartDto> salesChart = new ArrayList<>();

        // 시작 날짜부터 오늘까지 총 7일 반복
        for (int i = 0; i < 7; i++) {
            // 현재 조회할 날짜
            LocalDate date = chartStartDate.plusDays(i);
            // 조회 날짜의 시작 시간
            LocalDateTime startDate = date.atStartOfDay();
            // 다음 날 00:00을 조회 종료 시간으로 사용
            LocalDateTime endDate = date.plusDays(1).atStartOfDay();
            // 해당 날짜의 매출 합계 조회
            Long sales = orderItemRepository.sumSalesBetween(startDate, endDate);
            // 차트 데이터 생성
            salesChart.add(DashboardChartDto.builder()
                    // 프론트에서 날짜 형식 가공
                    .label(date.toString())
                    .value(sales)
                    .build()
            );
        }
        return salesChart;
    }


}