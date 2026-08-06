package org.spring.backend.commuTest;

import org.junit.jupiter.api.Test;
import org.spring.backend.community.dto.TabDto;
import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.TabEntity;
import org.spring.backend.community.repository.CategoryRepository;
import org.spring.backend.community.repository.TabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class TabTest {
    @Autowired
    TabRepository tabRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void insertTabCategory() {
        // Map<탭이름, List.of(카테고리이름)
        Map<String, List<String>> tabData = Map.of(
                "공지사항", List.of("공지사항"),
                "자유게시판", List.of("일상이야기", "식사게시판", "질문게시판"),
                "운동게시판", List.of("운동관련 질문", "자랑게시판", "팁 게시판"),
                "Q&A", List.of("Q&A 게시판")
        );
        // 데이터 저장
        for (Map.Entry<String, List<String>> entry : tabData.entrySet()) {
            String tabName = entry.getKey();

            // 탭 생성, 저장
            TabEntity tab = TabEntity.builder()
                    .tabName(tabName)
                    .adminOnly(tabName.equals("공지사항") ? true : false) //공지사항 관리자만 가능
                    .build();
            TabEntity savedTab = tabRepository.save(tab);

            // 카테고리 생성
            List<CategoryEntity> categories = entry.getValue().stream()
                    .map(catName -> CategoryEntity.builder()
                            .categoryName(catName)
                            .tabEntity(savedTab)
                            .build())
                    .toList();

            // 카테고리 저장
            categoryRepository.saveAll(categories);
        }
    }

    @Test
    void tabCategoryList() {
        //모든 리스트 읽기
        List<TabEntity> entity = tabRepository.findAll();
        for (TabEntity tab : entity) {
            System.out.println("탭 이름: " + tab.getTabName());
            List<CategoryEntity> categories = categoryRepository.findByTabEntity(tab);
            categories.forEach(c -> System.out.println(" - 카테고리: " + c.getCategoryName()));
        }
    }

    @Test
    void delete(){
        tabRepository.deleteById(Long.valueOf(47));
    }
}

