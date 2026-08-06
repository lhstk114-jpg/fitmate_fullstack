package org.spring.backend.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardProductDto {

    // 상품 상세 이동용 ID
    private Long id;

    // 상품명
    private String productName;

    // 상품 가격
    private Integer price;

    // 누적 판매 수량
    private Long salesCount;
}