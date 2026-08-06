package org.spring.backend.chatbot.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:ec2.exchange}")
    private String exchange;

    @Value("${rabbitmq.notification.routing.key:ec2.notification.#}")
    private String routingKey;

    //스케줄러 세팅
//    @Scheduled(cron = "0 10 * * * *")
//    public void sendDailyNotification(){
//        try{
//            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("a h:mm"));
//            //프론트에 그려질 html구조
//            String messageHtml = "<div class='msg>"+
//                    "<div class='head-img'><img src='/images/bell.png'></div>"+
//                    "<div class='message'>오늘의 알림입니다!</div>"+
//                    "<div class='audio'> "+
//                    " <audio controls autoplay muted id='alarmAudio'><source src='/audio/alarm.ogg' type='audio/ogg'> " +
//                    "<source src='/audio/alarm.mp3' type='audio/mpeg'> </audio></div>" +
//                    "<div class='time'>" + time + "</div>" +
//                    "</div>";
//            //와일드 카드(#) 문자를 실제 라우팅 경로 이름으로 변경
//            String actualRoutingKey = routingKey.contains("#") ? routingKey.replace("#","daily") : "ec2.notification.daily";
//            //RabbitMQ Exchange로 메시지 전송
//            rabbitTemplate.convertAndSend(exchange, actualRoutingKey, messageHtml);
//        }catch (Exception e){
//            System.err.println("스케줄러 에러 "+e.getMessage());
//        }
//    }
}
