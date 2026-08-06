package org.spring.backend.community.repository;

import org.spring.backend.community.entity.TabEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 탭 리포지토리
 * 별도의 커스텀 쿼리 없이 JpaRepository 기본 메서드(findAll, findById, save, deleteById 등)만 사용
 */
public interface TabRepository extends JpaRepository<TabEntity, Long>{
  
}
