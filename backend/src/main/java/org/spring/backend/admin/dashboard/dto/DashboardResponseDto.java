package org.spring.backend.admin.dashboard.dto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

    // 주요 현황
    private DashboardSummaryDto summary;

    // 회원 CRM
    private DashboardMemberDto member;

    // 관심사 분포 차트
    private List<DashboardChartDto> interestChart;

    // 게시글 등록 추이 차트
    private List<DashboardChartDto> communityChart;

    // 매출 추이 차트
    private List<DashboardChartDto> salesChart;

    // 회원 구독 현황 차트
    private List<DashboardChartDto> subscriptionChart;

    // 커뮤니티 리스트 TOP5
    private List<DashboardCommunityDto> communityList;

    // 상품 리스트 TOP5
    private List<DashboardProductDto> productList;
}
