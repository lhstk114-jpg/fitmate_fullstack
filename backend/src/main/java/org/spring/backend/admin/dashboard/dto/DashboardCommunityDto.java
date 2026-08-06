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
public class DashboardCommunityDto {

    // 게시글 상세 이동용 ID
    private Long id;

    // 게시글 제목
    private String title;

    // 카테고리명
    private String categoryName;

    // 조회수
    private Integer hit;
}