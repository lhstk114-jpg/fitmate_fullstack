package org.spring.backend.admin.popup.repository;

import org.spring.backend.admin.popup.entity.PopupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PopupRepository extends JpaRepository<PopupEntity, Long> {
    @Query("""
        SELECT p
        FROM PopupEntity p
        WHERE p.active = true
          AND p.startDate <= :now
          AND p.endDate >= :now
        ORDER BY p.sortOrder ASC
    """)
    List<PopupEntity> findActivePopup(LocalDateTime now);

    Page<PopupEntity> findByEndDateContaining(Pageable pageable, String search);

    Page<PopupEntity> findByStartDateContaining(Pageable pageable, String search);

    Page<PopupEntity> findByTitleContaining(Pageable pageable, String search);

    Page<PopupEntity> findByActive(Boolean active, Pageable pageable);

    Page<PopupEntity> findBySortOrder(Integer sortOrder, Pageable pageable);

// Popup 조회 흐름
// 1. 현재 시간(LocalDateTime.now())을 기준으로 조회
// 2. active = true 이고, startDate <= now <= endDate 조건을 만족하는 팝업 조회
// 3. sortOrder(우선순위) 오름차순으로 정렬
}
