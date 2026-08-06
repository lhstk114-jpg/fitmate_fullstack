package org.spring.backend.chatbot.repository;

import org.spring.backend.chatbot.entity.AnswerEntity;
import org.spring.backend.chatbot.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    // 카테고리 ID와 특정 이름으로 세부 정보 검색
    List<AnswerEntity> findByChatEntityIdAndName(Long chatId, String name);

    //특정 chatId에 속한 Answer들을 페이징으로 조회
    Page<AnswerEntity> findByChatEntityId(Long chatId, Pageable pageable);

    Page<AnswerEntity> findByChatEntityIdAndNameContaining(Long chatId,Pageable pageable, String search);

    Page<AnswerEntity> findByChatEntityIdAndContentContaining(Long chatId,Pageable pageable, String search);
}