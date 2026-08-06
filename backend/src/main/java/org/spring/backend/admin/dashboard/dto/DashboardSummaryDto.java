package org.spring.backend.admin.dashboard.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    // 전체 회원 수
    private Long totalMemberCount;

    // 등록된 전체 상품 수
    private Long totalProductCount;

    // 오늘 작성된 게시글 수
    private Long todayCommunityCount;

    // 오늘 매출
    private Long todaySales;

    // 이번 달 누적 매출
    private Long currentMonthSales;

    // 지난 달 매출
    private Long lastMonthSales;
}