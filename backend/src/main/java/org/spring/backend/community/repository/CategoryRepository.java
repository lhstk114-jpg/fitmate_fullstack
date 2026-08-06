package org.spring.backend.community.repository;

import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.TabEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 카테고리 리포지토리
 */
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>{
    // 카테고리와 소속 탭을 함께 즉시 로딩(JOIN FETCH)해서 조회 (N+1 문제 방지용, 현재 사용처는 확인 필요)
    @Query("SELECT c FROM CategoryEntity c JOIN FETCH c.tabEntity")
    List<CategoryEntity> findAllWithTab();

    // 특정 탭에 속한 카테고리만 조회
    List<CategoryEntity> findByTabEntity(TabEntity tab);
}
