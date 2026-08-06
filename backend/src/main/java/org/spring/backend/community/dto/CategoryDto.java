package org.spring.backend.community.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 DTO
 * 프론트: CommunityLeft.jsx / CommunityInsert.jsx / AdminCommunity.jsx 등에서
 * tabId로 필터링해 특정 탭에 속한 카테고리만 골라 쓰는 형태로 사용됨
 */
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
  private Long id;

  // 카테고리 이름 (예: "자유게시판", "FAQ", "QNA" 등 - 프론트에서 이름으로 특수 로직을 분기하는 곳이 있으므로 이름 변경 시 주의)
  private String categoryName;

  // 이 카테고리가 속한 탭의 id
  private Long tabId;
}
