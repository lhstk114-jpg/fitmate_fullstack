package org.spring.backend.commuTest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.entity.TabEntity;
import org.spring.backend.community.repository.CategoryRepository;
import org.spring.backend.community.repository.CommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@SpringBootTest
public class CommunityTest {
    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CategoryRepository categoryRepository;



    @Test
    void insert(){
        CategoryEntity fixedCategory = categoryRepository.findById(Long.valueOf(1))
                .orElseThrow(() -> new RuntimeException("구매 카테고리가 없습니다."));
        for (int i = 0; i < 10; i++) {
            communityRepository.save(CommunityEntity.builder()
                    .title("공지사항 예시입니다" + i)
                    .userName("admin")
                    .content("공지사항 예시입니다" + i)
                    .categoryEntity(fixedCategory)
                    .hasFile(0)
                    .hit(0)
                    .reply(0)
                    .build());
        }
    }

    @Test
    void list() {
        //모든 리스트 읽기
        List<CategoryEntity> entity = categoryRepository.findAll();
        for (CategoryEntity cat : entity) {
            System.out.println("카테고리 이름: " + cat.getCategoryName());
            Page<CommunityEntity> com = communityRepository.findByCategoryEntity_Id(cat.getId(), Pageable.unpaged());
            com.forEach(c -> System.out.println("제목: " + c.getTitle()));
        }
    }

    @Test
    void delete(){
        communityRepository.deleteById(Long.valueOf(100));
    }
}
