package org.spring.backend.community.repository;

import org.spring.backend.community.entity.CommunityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 리포지토리
 * JpaSpecificationExecutor를 함께 상속받아 findCommunityList(동적 조건 조합)에서 Specification 기반 동적 쿼리를 사용
 */
public interface CommunityRepository extends JpaRepository<CommunityEntity, Long>,JpaSpecificationExecutor<CommunityEntity>{

    // 특정 카테고리의 게시글 페이지 조회 (카테고리 연관관계 기준)
    Page<CommunityEntity> findByCategoryEntity_Id(Long categoryId, Pageable pageable);

    // 특정 탭의 게시글 페이지 조회 (비정규화된 tabId 컬럼 기준 - 연관관계가 아닌 저장된 값으로 조회)
    Page<CommunityEntity> findByTabId(Long tabId, Pageable pageable);


    // 공지사항만 최신순 TOP 5
    List<CommunityEntity> findTop5ByTabNameOrderByCreateTimeDesc(
            String TabName
    );

    // 비회원용: notice 제외하고 조회수 높은순 TOP 5
    List<CommunityEntity> findTop5ByTabNameNotOrderByHitDesc(
            String TabName
    );

    // 로그인 회원용: 관심사 카테고리 기준 조회수 높은순 TOP 5
    List<CommunityEntity> findTop5ByTabNameOrderByHitDesc(
            String TabName
    );
    //제목 검색
    Page<CommunityEntity> findByTitleContaining(Pageable pageable, String search);
    //내용검색
    Page<CommunityEntity> findByContentContaining(Pageable pageable, String search);
    //작성자 검색
    Page<CommunityEntity> findByUserNameContaining(Pageable pageable, String search);

    // 특정 탭의 조회수 top5
    List<CommunityEntity> findTop5ByCategoryEntity_TabEntity_IdOrderByHitDesc(Long tabId);

    // 특정 탭의 최신순 top5 (공지사항용)
    List<CommunityEntity> findTop5ByCategoryEntity_TabEntity_IdOrderByCreateTimeDesc(Long tabId);

    // 특정 탭 제외 전체 조회수 top5
    @Query("""
    SELECT c FROM CommunityEntity c
    WHERE c.tabId <> :tabId
      AND UPPER(c.categoryName) NOT LIKE '%QNA%'
    ORDER BY c.hit DESC
    LIMIT 5
    """)
    List<CommunityEntity> findTop5ByCategoryEntity_TabEntity_IdNotOrderByHitDesc(@Param("tabId") Long tabId);

    
     //오늘 작성된 글 개수
    Long countByCreateTimeGreaterThanEqualAndCreateTimeLessThanAndTabNameNot(LocalDateTime startOfToday, LocalDateTime startOfTomorrow, String tabName);

    /**
     * ✅ 신규 추가: 탭 이름이 수정되면, 그 탭에 속한 모든 게시글의 비정규화된 tabName 컬럼도 함께 갱신.
     * (기존에는 TabServiceImpl.tabUpdate가 TabEntity의 이름만 바꾸고 게시글 쪽 tabName은 손대지 않아서,
     *  탭 이름을 바꿔도 예전에 작성된 글들의 tabName은 옛 이름 그대로 남아있었음)
     * tabId 기준으로 일괄 업데이트하며, 카테고리와 무관하게 해당 탭 소속 게시글 전체가 대상.
     */
    @Modifying
    @Query("UPDATE CommunityEntity c SET c.tabName = :tabName WHERE c.tabId = :tabId")
    void updateTabNameByTabId(@Param("tabId") Long tabId, @Param("tabName") String tabName);

    /**
     * ✅ 신규 추가: 카테고리 이름이 수정되면, 그 카테고리에 속한 모든 게시글의 비정규화된 categoryName 컬럼도 함께 갱신.
     * categoryEntity 연관관계의 id 기준으로 일괄 업데이트.
     */
    @Modifying
    @Query("UPDATE CommunityEntity c SET c.categoryName = :categoryName WHERE c.categoryEntity.id = :categoryId")
    void updateCategoryNameByCategoryId(@Param("categoryId") Long categoryId, @Param("categoryName") String categoryName);
}
