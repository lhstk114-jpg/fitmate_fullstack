package org.spring.backend.chatbot.dto;

import lombok.*;
import org.spring.backend.chatbot.entity.AnswerEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerDto {
    private Long id;

    private String name;

    private String content;

    public static AnswerDto toAnswerDtoSummary(AnswerEntity answerEntity){
        if (answerEntity == null) return null;
        return AnswerDto.builder()
                .id(answerEntity.getId())
                .name(answerEntity.getName())
                .content(answerEntity.getContent())
                .build();
    }
}
