package org.spring.backend.chatbot.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.spring.backend.chatbot.dto.AnswerDto;
import org.spring.backend.chatbot.dto.ChatDto;
import org.spring.backend.chatbot.entity.AnswerEntity;
import org.spring.backend.chatbot.entity.ChatEntity;
import org.spring.backend.chatbot.repository.AnswerRepository;
import org.spring.backend.chatbot.repository.ChatRepository;
import org.spring.backend.chatbot.service.ChatBotAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatBotAdminServiceImpl implements ChatBotAdminService {
    private final AnswerRepository answerRepository;
    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public void insertChat(ChatDto chatDto) {
        //대주제가 존재하는지 체크
        Optional<ChatEntity> optionalChatEntity = chatRepository.findBySearch(chatDto.getSearch());
        if(optionalChatEntity.isPresent()){
            throw new IllegalArgumentException("이미 존재하는 질문입니다.");
        }
        //대주제 저장
        chatRepository.save(ChatEntity.builder()
                .resStr(chatDto.getResStr())
                .search(chatDto.getSearch())
                .keywordType(chatDto.getKeywordType())
                .build());
    }

    @Override
    @Transactional
    public void insertAnswer(AnswerDto answerDto, Long chatId) {
        //부모 ChatEntity 존재 여부 확인 (외래키 매핑용)
        ChatEntity chatEntity = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리(Chat)입니다."));

        //자식 AnswerEntity 생성 및 저장
        answerRepository.save(AnswerEntity.builder()
                .name(answerDto.getName())
                .content(answerDto.getContent())
                .chatEntity(chatEntity) //부모 엔티티 매핑
                .build());
    }

    @Override
    @Transactional
    public void updateChat(ChatDto chatDto) {
        ChatEntity chatEntity = chatRepository.findById(chatDto.getId())
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 Chat입니다."));
        //대주제 저장
        chatRepository.save(ChatEntity.builder()
                .id(chatEntity.getId())
                .resStr(chatDto.getResStr())
                .search(chatDto.getSearch())
                .keywordType(chatDto.getKeywordType())
                .build());
    }

    @Override
    @Transactional
    public void updateAnswer(AnswerDto answerDto, Long chatId) {
        AnswerEntity answerEntity = answerRepository.findById(answerDto.getId())
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 Answer입니다."));
        //부모 ChatEntity 존재 여부 확인 (외래키 매핑용)
        ChatEntity chatEntity = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리(Chat)입니다."));
        //대주제 저장
        answerRepository.save(AnswerEntity.builder()
                .id(answerEntity.getId())
                .name(answerDto.getName())
                .content(answerDto.getContent())
                .chatEntity(chatEntity) //부모 엔티티 매핑
                .build());
    }

    @Override
    @Transactional
    public void deleteChat(Long id) {
        chatRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 Chat입니다."));
        chatRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long id) {
        answerRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 Answer입니다."));
        answerRepository.deleteById(id);
    }

    @Override
    public ChatDto detailChat(Long id) {
        ChatEntity chatEntity = chatRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 Chat입니다."));
        return ChatDto.toChatDtoSummary(chatEntity);
    }

    @Override
    public AnswerDto detailAnswer(Long id) {
        AnswerEntity answerEntity = answerRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 Answer입니다."));
        return AnswerDto.toAnswerDtoSummary(answerEntity);
    }

    @Override
    public Page<ChatDto> getChatList(Pageable pageable, String subject, String search) {
        if(subject==null||subject.isBlank()||search==null||search.isBlank()){
            return chatRepository.findAll(pageable).map(ChatDto::toChatDtoSummary);
        }
        Page<ChatEntity> chatEntities = switch (subject) {
            case "search" -> chatRepository.findBySearchContaining(pageable, search);
            case "resStr" -> chatRepository.findByResStrContaining(pageable, search);
            default -> chatRepository.findAll(pageable);
        };
        return chatEntities.map(ChatDto::toChatDtoSummary);
    }

    @Override
    public Page<AnswerDto> getAnswerListByChatId(Long chatId, Pageable pageable, String subject, String search) {
        if(subject==null||subject.isBlank()||search==null||search.isBlank()){
            return answerRepository.findByChatEntityId(chatId, pageable).map(AnswerDto::toAnswerDtoSummary);
        }
        Page<AnswerEntity> chatEntities = switch (subject) {
            case "name" -> answerRepository.findByChatEntityIdAndNameContaining(chatId, pageable, search);
            case "content" -> answerRepository.findByChatEntityIdAndContentContaining(chatId, pageable, search);
            default -> answerRepository.findByChatEntityId(chatId, pageable);
        };
        return chatEntities.map(AnswerDto::toAnswerDtoSummary);
    }
}
