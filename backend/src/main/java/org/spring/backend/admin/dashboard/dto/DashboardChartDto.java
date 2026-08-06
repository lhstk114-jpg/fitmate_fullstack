package org.spring.backend.admin.dashboard.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartDto {

    // 차트의 X축 값 -> 항목
    private String label;

    // 차트의 Y축 값 -> 수치
    private Long value;
}