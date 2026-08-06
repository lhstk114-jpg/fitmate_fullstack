package org.spring.backend.chatbot.config;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Answer {
    //응답
    private String answer;
    private String message;
}
