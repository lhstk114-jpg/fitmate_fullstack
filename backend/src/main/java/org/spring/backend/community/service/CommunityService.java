package org.spring.backend.community.service;

import org.spring.backend.community.dto.CommunityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 게시글(커뮤니티) 서비스 인터페이스
 * 구현체: CommunityServiceImpl
 */
public interface CommunityService {
  // 게시글 작성 (관리자 전용 탭이면 관리자 권한 검증)
  void communityInsert(CommunityDto communityDto, String userEmail);

  // 전체 게시글 목록 (제목/내용/작성자 검색 지원)
  Page<CommunityDto> communityList(Pageable pageable, String subject, String search);

  // 게시글 수정 (작성자 본인 또는 관리자만 가능)
  void communityUpdate(Long id, CommunityDto communityDto, String  userEmail);

  // 게시글 삭제 (작성자 본인 또는 관리자만 가능, 일반 사용자용 엔드포인트에서 사용)
  void communityDelete(Long id, String userEmail);

  // 게시글 삭제 (관리자 전용)
  // ✅ 수정: 요청자 이메일을 받아 서비스단에서도 ADMIN 권한을 검증하도록 시그니처에 userEmail 추가
  //    (기존에는 인자가 id뿐이라 호출하는 쪽에서 권한 검증을 깜빡해도 막을 방법이 없었음)
  void adminDelete(Long id, String userEmail);

  // 게시글 상세 조회
  CommunityDto communityDetail(Long id, String userEmail);

  // 조회수 1 증가
  void updateHit(Long id);

  // 커뮤니티 메인 페이지용 데이터 (탭별 top5, 전체 top5, 탭 목록)
  Map<String , Object> mainList();

  // 탭/카테고리/키워드로 필터링된 게시글 목록 (CommunityList, AdminCommunity에서 사용하는 /tclist)
  Page<CommunityDto> findCommunityList(Long tabId, Long categoryId, String keyword, Pageable pageable);
}
