package org.spring.backend.commuTest;

import org.junit.jupiter.api.Test;
import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.entity.CommunityReplyEntity;
import org.spring.backend.community.repository.CategoryRepository;
import org.spring.backend.community.repository.CommunityReplyRepository;
import org.spring.backend.community.repository.CommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@SpringBootTest
public class CommunityReplyTest {
    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CommunityReplyRepository communityReplyRepository;



    @Test
    void insert(){
        CommunityEntity com = communityRepository.findById(Long.valueOf(101))
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));
        for (int i = 0; i < 10; i++) {
            communityReplyRepository.save(CommunityReplyEntity.builder()
                    .content("구매" + i)
                    .userEmail("email"+i)
                    .userName("writer" + i)
                    .communityEntity(com)
                    .build());
        }
    }

    @Test
    void list() {
        //모든 리스트 읽기
        List<CommunityEntity> entity = communityRepository.findAll();
        for (CommunityEntity com : entity) {
            System.out.println("카테고리 이름: " + com.getTitle());
            List<CommunityReplyEntity> rep = communityReplyRepository.findAllByCommunityId(com.getId());
            rep.forEach(c -> System.out.println("제목: " + c.getContent()));
        }
    }
    
    @Test
    void delete(){
        communityReplyRepository.deleteById(Long.valueOf(10));
    }
}
