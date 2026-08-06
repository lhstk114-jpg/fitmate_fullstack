package org.spring.backend.community.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.backend.community.entity.TabEntity;

/**
 * 탭 DTO
 * 프론트: TabDetail.jsx(수정 시 categoryList 함께 전송), TabInsert.jsx(생성 시 categoryList 함께 전송)
 */
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TabDto {
  private Long id;

  private String tabName;

  private Boolean adminOnly;

  // 탭에 속한 카테고리 목록 (탭 상세/수정/생성 화면에서 함께 다뤄짐)
  private List<CategoryDto> categoryList;

  // 카테고리 이름만 뽑은 리스트 (categoryList와 별도로 존재 - 용도는 서비스단 확인 필요, 실제 사용처가 명확하지 않음)
  private List<String> categoryNames;

  // 엔티티 → DTO 변환: 탭의 기본 정보만 옮김 (categoryList는 별도로 채워야 함, 여기서는 세팅하지 않음)
  public static TabDto toTabDto(TabEntity entity) {
    return TabDto.builder()
            .id(entity.getId())
            .tabName(entity.getTabName())
            .adminOnly(entity.getAdminOnly())
            .build();
  }
}
