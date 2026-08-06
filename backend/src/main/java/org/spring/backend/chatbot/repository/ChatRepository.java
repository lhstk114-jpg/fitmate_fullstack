package org.spring.backend.chatbot.repository;

import org.spring.backend.chatbot.entity.ChatEntity;
import org.spring.backend.chatbot.enumtype.KeywordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    // 키워드로 대화 카테고리 검색
    Optional<ChatEntity> findBySearch(String search);

    Page<ChatEntity> findBySearchContaining(Pageable pageable, String search);

    Page<ChatEntity> findByResStrContaining(Pageable pageable, String search);

    Optional<ChatEntity> findBySearchAndKeywordType(String noun, KeywordType keywordType);
}