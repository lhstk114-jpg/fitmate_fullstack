package org.spring.backend.community.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spring.backend.community.dto.CategoryDto;
import org.spring.backend.community.dto.TabDto;
import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.TabEntity;
import org.spring.backend.community.repository.CategoryRepository;
import org.spring.backend.community.repository.CommunityRepository;
import org.spring.backend.community.repository.TabRepository;
import org.spring.backend.community.service.TabService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 탭/카테고리 서비스 구현체
 * ✅ tabUpdate()에서 탭/카테고리 이름을 바꿀 때, CommunityRepository의 벌크 업데이트 쿼리를 호출해
 *    해당 탭/카테고리에 속한 기존 게시글들의 비정규화된 tabName/categoryName도 함께 갱신하도록 수정함.
 *    (기존에는 TabEntity/CategoryEntity만 갱신하고 CommunityEntity 쪽은 그대로 두어,
 *     탭/카테고리 이름을 바꾸면 새 글에는 새 이름이 붙지만 기존 글은 예전 이름으로 남아있는 문제가 있었음)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TabServiceImpl implements TabService {
    private final TabRepository tabRepository;
    private final CategoryRepository categoryRepository;
    // ✅ 신규 추가: 탭/카테고리 이름 변경 시 관련 게시글의 비정규화된 이름 컬럼도 함께 갱신하기 위해 주입
    private final CommunityRepository communityRepository;

/**
 * 탭 + 카테고리 일괄 생성
 * 여러 탭을 순회하며 각 탭을 저장하고, 그 탭에 속한 카테고리들도 함께 저장
 * (TabInsert.jsx에서 여러 탭/카테고리를 한 화면에서 구성해 한 번에 전송하는 구조와 대응)
 */
@Override
@Transactional
public void insertTab(List<TabDto> tabDtoList) { // 파라미터를 List로 받습니다.
    
    // 1. 전달받은 탭 리스트를 하나씩 순회합니다.
    for (TabDto tabDto : tabDtoList) {
        
        // 탭 엔티티 생성 및 저장
        TabEntity tab = new TabEntity();
        tab.setTabName(tabDto.getTabName());
        tab.setAdminOnly(tabDto.getAdminOnly());
        tabRepository.save(tab);

        // 2. 해당 탭의 카테고리 리스트를 순회합니다.
        if (tabDto.getCategoryList() != null) {
            for (CategoryDto catDto : tabDto.getCategoryList()) {
                CategoryEntity category = new CategoryEntity();
                category.setCategoryName(catDto.getCategoryName());
                category.setTabEntity(tab);
                categoryRepository.save(category);
            }
        }
    }
}

    /**
     * 전체 탭 목록 조회
     * 각 탭마다 categoryList(카테고리 상세 정보)와 categoryNames(이름만 뽑은 리스트) 둘 다 채워서 반환
     */
    @Override
    public List<TabDto> tabList() {
        List<TabEntity> tabEntities = tabRepository.findAll();

        return tabEntities.stream().map(el -> {
            List<CategoryDto> dtos = el.getCategoryList().stream()
                    .map(cat -> CategoryDto.builder()
                            .id(cat.getId())
                            .categoryName(cat.getCategoryName())
                            .build())
                    .toList();

            List<String> names = el.getCategoryList().stream()
                    .map(CategoryEntity::getCategoryName)
                    .toList();

            return TabDto.builder()
                    .id(el.getId())
                    .tabName(el.getTabName())
                    .adminOnly(el.getAdminOnly())
                    .categoryList(dtos)
                    .categoryNames(names)
                    .build();
        }).toList();
    }

    /**
     * 탭 수정 (이름/adminOnly 갱신 + 카테고리 목록 동기화)
     * 카테고리 동기화 방식(TabDetail.jsx의 편집 방식과 대응):
     * - dto에 id가 있는 카테고리 → 기존 카테고리를 찾아 이름만 갱신
     * - dto에 id가 없는 카테고리 → 신규 카테고리로 생성
     * - 기존 카테고리 리스트를 통째로 비우고(clear) 새 리스트로 교체(addAll) →
     *   orphanRemoval=true이므로, 새 리스트에 없는 기존 카테고리는 자동으로 DB에서 삭제됨
     *   (해당 카테고리의 게시글도 cascade로 함께 삭제되니 주의)
     * ✅ 수정: 탭 이름이 바뀌면 communityRepository.updateTabNameByTabId로 관련 게시글의 tabName도 함께 갱신.
     *    카테고리 이름이 바뀐 기존 카테고리(dto.getId() != null이면서 이름이 실제로 달라진 경우)에 대해서도
     *    communityRepository.updateCategoryNameByCategoryId로 관련 게시글의 categoryName을 함께 갱신.
     *    (새로 추가되는 카테고리는 아직 게시글이 없으므로 갱신 대상에서 자연히 제외됨)
     */
    @Override
    @Transactional
    public void tabUpdate(TabDto tabDto) {
        // 1. 탭 조회
        TabEntity tab = tabRepository.findById(tabDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("탭이 존재하지 않습니다"));

        // 탭 이름이 실제로 바뀌었는지 미리 확인 (바뀐 경우에만 게시글 벌크 업데이트 실행)
        boolean tabNameChanged = tabDto.getTabName() != null && !tabDto.getTabName().equals(tab.getTabName());

        // 2. 탭 이름 수정
        tab.setTabName(tabDto.getTabName());
        tab.setAdminOnly(tabDto.getAdminOnly());

        // 3. 기존 카테고리 리스트 가져오기
        List<CategoryEntity> existingCategories = tab.getCategoryList();

        // 4. 요청받은 카테고리 DTO를 기반으로 리스트 동기화 (변경된 기존 카테고리의 id/새 이름도 함께 수집)
        List<Long[]> changedCategoryIdsPlaceholder = new java.util.ArrayList<>(); // (실제 갱신 목록은 아래 map에서 채움)
        List<java.util.AbstractMap.SimpleEntry<Long, String>> changedCategories = new java.util.ArrayList<>();

        List<CategoryEntity> updatedCategories = tabDto.getCategoryList().stream()
                .map(dto -> {
                    if (dto.getId() != null) {
                        // 기존 카테고리 업데이트
                        CategoryEntity existing = existingCategories.stream()
                                .filter(c -> c.getId().equals(dto.getId()))
                                .findFirst()
                                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));

                        // 이름이 실제로 바뀐 경우에만 벌크 업데이트 대상으로 기록
                        if (dto.getCategoryName() != null && !dto.getCategoryName().equals(existing.getCategoryName())) {
                            changedCategories.add(new java.util.AbstractMap.SimpleEntry<>(dto.getId(), dto.getCategoryName()));
                        }

                        existing.setCategoryName(dto.getCategoryName());
                        return existing;
                    } else {
                        // 새로운 카테고리 추가 (아직 게시글이 없으므로 벌크 업데이트 대상 아님)
                        return CategoryEntity.builder()
                                .categoryName(dto.getCategoryName())
                                .tabEntity(tab)
                                .build();
                    }
                }).toList();

        // 5. 기존 리스트를 지우고 새로운 리스트로 교체 (orphanRemoval에 의해 삭제됨)
        existingCategories.clear();
        existingCategories.addAll(updatedCategories);

        tabRepository.save(tab);

        // 6. ✅ 탭 이름이 바뀌었으면, 이 탭 소속 게시글들의 비정규화된 tabName도 함께 갱신
        if (tabNameChanged) {
            communityRepository.updateTabNameByTabId(tab.getId(), tab.getTabName());
        }

        // 7. ✅ 이름이 바뀐 기존 카테고리들에 대해, 관련 게시글들의 비정규화된 categoryName도 함께 갱신
        for (var entry : changedCategories) {
            communityRepository.updateCategoryNameByCategoryId(entry.getKey(), entry.getValue());
        }
    }

    // 탭 삭제 (존재 여부 확인 후 삭제, TabEntity.categoryList가 cascade + orphanRemoval이므로 하위 카테고리도 함께 삭제됨)
    @Override
    public void tabDelete(Long id) {
        Optional<TabEntity> optionalTabEntity = tabRepository.findById(id);
        if (optionalTabEntity.isEmpty()) {
            throw new IllegalArgumentException("탭이 존재하지 않습니다");
        }
        tabRepository.deleteById(id);
    }


    // 탭 상세 조회 (하위 카테고리 목록/이름 리스트 포함, TabDetail.jsx의 수정 화면 초기값으로 사용)
    @Override
    public TabDto tabDetail(Long id) {
        TabEntity tabEntity = tabRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("탭이 존재하지 않습니다"));

        List<CategoryDto> dtos = tabEntity.getCategoryList().stream()
                .map(cat -> CategoryDto.builder()
                        .id(cat.getId())
                        .categoryName(cat.getCategoryName())
                        .build())
                .collect(Collectors.toList());

        List<String> names = tabEntity.getCategoryList().stream()
                .map(CategoryEntity::getCategoryName)
                .toList();

        return TabDto.builder()
                .id(tabEntity.getId())
                .tabName(tabEntity.getTabName())
                .adminOnly(tabEntity.getAdminOnly()) // ★ 추가
                .categoryList(dtos)
                .categoryNames(names)
                .build();
    }

// 전체 카테고리 목록 조회 (탭 구분 없이 전체를 평탄하게 반환, 각 항목에 tabId를 채워 프론트가 클라이언트단에서 탭별로 필터링)
@Override
public List<CategoryDto> categoryList() {
    return categoryRepository.findAll().stream().map(c -> {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setCategoryName(c.getCategoryName());
        if (c.getTabEntity() != null) {
            dto.setTabId(c.getTabEntity().getId());
        }
        return dto;
    }).toList();
}
}
