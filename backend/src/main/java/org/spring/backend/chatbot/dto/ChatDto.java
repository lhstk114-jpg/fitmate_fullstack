package org.spring.backend.chatbot.dto;

import lombok.*;
import org.spring.backend.chatbot.entity.ChatEntity;
import org.spring.backend.chatbot.enumtype.KeywordType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDto {
    private Long id;

    private String resStr; //답변

    private String search; //검색단어

    private KeywordType keywordType;

    private List<AnswerDto> answerList;

    public static ChatDto toChatDtoSummary(ChatEntity chatEntity) {
        return ChatDto.builder()
                .id(chatEntity.getId())
                .resStr(chatEntity.getResStr())
                .search(chatEntity.getSearch())
                .keywordType(chatEntity.getKeywordType())
                .build();
    }
}
