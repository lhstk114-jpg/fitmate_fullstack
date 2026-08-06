package org.spring.backend.chatbot.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.spring.backend.chatbot.config.Answer;
import org.spring.backend.chatbot.config.Question;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Receiver {
    private final SimpMessagingTemplate simpMessagingTemplate;

    //요청에 대한 응답
    @RabbitListener(queues = "${rabbitmq.queue.question}")
    public void receiveQuestion(String question){
        String src = "<div class='msg>"+
                "<div class='head-img'><img src='/imgages/chat.png'>" +
                "<span style='color:#f80;font-weight:bold;'>RabbitMQ</span></div>"+
                "<div class='message'>" + question + "에 대한 답자입니다.</div>"+
                "</div>";
        simpMessagingTemplate.convertAndSend("topic/question",
                Answer.builder().message(src).build());
    }
}
