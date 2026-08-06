package org.spring.backend.chatbot.service;

import org.spring.backend.chatbot.dto.AnswerDto;
import org.spring.backend.chatbot.dto.ChatDto;
import org.spring.backend.chatbot.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatBotAdminService {
    //대주제 등록
    void insertChat(ChatDto chatDto);

    //답변 등록
    void insertAnswer(AnswerDto answerDto, Long chatId);
    //대주제 수정
    void updateChat(ChatDto chatDto);

    //답변 수정
    void updateAnswer(AnswerDto answerDto, Long chatId);

    //대주제 삭제
    void deleteChat(Long id);

    //답변 삭제
    void deleteAnswer(Long id);

    //대주제 상세
    ChatDto detailChat(Long id);

    //답변 상세
    AnswerDto detailAnswer(Long id);

    //대주제(Chat) 목록 자체를 페이징 조회할 경우
    Page<ChatDto> getChatList(Pageable pageable, String subject, String search);

    //특정 Chat(대주제)에 속한 세부 답변 목록 페이징 조회
    Page<AnswerDto> getAnswerListByChatId(Long chatId, Pageable pageable, String subject, String search);

}
