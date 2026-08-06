package org.spring.backend.chatbot.config;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    //질의 -> 클라이언트 요청
    private long key;
    private String name;
    private String question;
    private String content;
}
