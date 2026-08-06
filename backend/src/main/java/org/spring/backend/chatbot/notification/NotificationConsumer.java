package org.spring.backend.chatbot.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    //RabbitMQ 수신 데이터 -> WebSocket전송데이터 -> 프론트
    private final SimpMessagingTemplate simpMessagingTemplate;

    //RabbitMQ Exchange
    @RabbitListener(queues = "${rabbitmq.queue.notification}",
    containerFactory = "myFactory")
    public void receiveNotification(Message message){
        try{
            //raw바이트 배열을 UTF-8문자열 데이터로 수신
            String messageHtml = new String(message.getBody(), StandardCharsets.UTF_8);
            //Jackson컨버터를 거치며 앞뒤에 들어간 이중 따옴표(")정제 가공
            if(messageHtml.startsWith("\"") && messageHtml.endsWith("\"")){
                messageHtml = messageHtml.substring(1, messageHtml.length()-1);
            }
            //문자열 내부에 유니코드 형태나 이모지가 있다면 완전히 제거
            messageHtml = messageHtml.replace("\\uD83D\\uDD14","")
                    .replace("🔔","");
            //자바스크립트 속 JSON 파싱 규칙(body.message)에 맞게 반환 Map 매핑
            Map<String, String> response = new HashMap<>();
            response.put("message",messageHtml);
            //프론트의 /topic/notification 채널로 웹소켓 브로드캐스팅 전송(WebSocketConfig)
            simpMessagingTemplate.convertAndSend("/topic/notification",response);
        } catch (Exception e) {
            System.err.println("[Counsumer 에러]알림 문자열 가공 및 전송 실패 "+e.getMessage());
        }
    }
}
