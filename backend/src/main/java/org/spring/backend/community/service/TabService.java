package org.spring.backend.community.service;

import org.spring.backend.community.dto.CategoryDto;
import org.spring.backend.community.dto.TabDto;

import java.util.List;

/**
 * 탭/카테고리 서비스 인터페이스
 * 구현체: TabServiceImpl
 */
public interface TabService {
  // 탭 여러 개를 카테고리와 함께 한 번에 생성 (TabInsert.jsx)
  void insertTab(List<TabDto> tabDtoList);

  // 전체 탭 목록 조회 (각 탭에 categoryList 포함)
  List<TabDto> tabList();

  // 탭 수정 (이름, adminOnly, 카테고리 목록 동기화)
  void tabUpdate(TabDto tabDto);

  // 탭 삭제
  void tabDelete(Long id);

  // 탭 상세 조회 (하위 카테고리 목록 포함)
  TabDto tabDetail(Long id);

  // 전체 카테고리 목록 조회 (탭 구분 없이 전체, 프론트에서 tabId로 클라이언트단 필터링)
  List<CategoryDto> categoryList();

}
