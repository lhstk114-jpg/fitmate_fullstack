package org.spring.backend.chatbot.message;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BotMessage {
    //메세지 내용
    private String content;
    //메세지 시간
    private String time;
}
