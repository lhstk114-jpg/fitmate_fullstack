package org.spring.backend.chatbot.controller;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import org.spring.backend.chatbot.dto.StompAnalyzeRequest;
import org.spring.backend.chatbot.dto.StompAnalyzeResponse;
import org.spring.backend.chatbot.entity.AnswerEntity;
import org.spring.backend.chatbot.entity.ChatEntity;
import org.spring.backend.chatbot.enumtype.KeywordType;
import org.spring.backend.chatbot.message.BotMessage;
import org.spring.backend.chatbot.message.ClientMessage;
import org.spring.backend.chatbot.repository.AnswerRepository;
import org.spring.backend.chatbot.repository.ChatRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ChatBotController {
    //komoran
    private final Komoran komoran;
    private final ChatRepository chatRepository;
    private final AnswerRepository answerRepository;

    //rabbitMQ
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:ec2.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key.question:question.key}")
    private String routingkey;

    //RabbitMQ 전송 + 챗봇 응답
    @MessageMapping("/bot")
    @SendTo("/topic/question")
    //LAZY로딩(1:N 관계 맺은 엔티티들) 유지를 위해 트랜잭션 추가
    @Transactional(readOnly = true)
    public StompAnalyzeResponse rabbitChat(StompAnalyzeRequest request) {
        String content = (request.content() == null) ? "" : request.content().strip();
        //komoran 분석기로 명사 추출(message에 들어있는 것들 기준)
        KomoranResult analyzeResult = komoran.analyze(content);
        //추출한 명사들을 List로 저장
        List<String> nouns = analyzeResult.getNouns();

        System.out.println("Komoran 추출 명사 목록: " + nouns);

        String systemMessage = "질문을 분류하지 못했습니다.";
        //질문 리스트를 저장할 배열
        List<String> answerList = new ArrayList<>();

        //대주제 선점용 타겟 오브젝트(질문의 주제 등)
        ChatEntity matchedChat = null;

        //"서울 날씨" 처럼 대주제 키워드 포함 여부 DB 매칭
        for (String noun : nouns) {
            Optional<ChatEntity> chatOpt = chatRepository.findBySearchAndKeywordType(noun, KeywordType.CATEGORY);

            if (chatOpt.isPresent()) {
                matchedChat = chatOpt.get(); // "상품" 매칭 성공!
                break;
            }
        }
        //대주제가 아닌 세부키워드 포함 여부 확인
        if (matchedChat == null) {
            for (String noun : nouns) {
                var chatOpt = chatRepository.findBySearchAndKeywordType(noun, KeywordType.ACTION);
                System.out.println(chatOpt);
                if (chatOpt.isPresent()) {
                    matchedChat = chatOpt.get();
                    break; // 세부 행동 키워드 매칭
                }
            }
        }
        //비즈니스 검색 로직 분기
        if (matchedChat != null) {
            systemMessage = matchedChat.getResStr(); // 시스템메시지에 어떤걸 물어봤는지 저장
            boolean hasDetailMatch = false;

            //함께 추출된 명사들 중 세부 항목명('서울' 등)이 있는지 검색
            for (String noun : nouns) {
                // 대주제 검색 키워드 자체는 세부 항목이 아니므로 제외
                if (!noun.equals(matchedChat.getSearch())) {
                    List<AnswerEntity> details = answerRepository.findByChatEntityIdAndName(matchedChat.getId(), noun);
                    if (!details.isEmpty()) {
                        for (AnswerEntity answer : details) {
                            answerList.add("[" + answer.getName() + "] " + answer.getContent());
                        }
                        hasDetailMatch = true;
                    }
                }
            }

            //"날씨 알려줘" 처럼 세부 명사 매칭이 전혀 없는 경우 카테고리 전체 목록 로드
            if (!hasDetailMatch && matchedChat.getAnswerEntities() != null) {
                answerList.add("원하시는 세부 항목을 함께 입력해주세요.");
                for (AnswerEntity answer : matchedChat.getAnswerEntities()) {
                    answerList.add("[" + answer.getName() + "] ");
                }
            }

        } else {
            //추출된 명사 중 매칭되는 대주제가 전혀 없을 경우 '기타' 카테고리 로드
            var etcOpt = chatRepository.findBySearch("기타");
            if (etcOpt.isPresent()) {
                ChatEntity etcChat = etcOpt.get();
                systemMessage = etcChat.getResStr(); // "날씨,상품,운동 이외의 질문입니다."

                // '기타' 카테고리에 묶여있는 기본 답변이 있다면 함께 표출
                if (etcChat.getAnswerEntities() != null) {
                    for (AnswerEntity answer : etcChat.getAnswerEntities()) {
                        answerList.add(answer.getContent());
                    }
                }
            } else {
                systemMessage = "날씨,상품,운동 이외의 질문입니다.";
                answerList.add("제공할 수 있는 답변 카테고리가 존재하지 않습니다.");
            }
        }
        rabbitTemplate.convertAndSend(exchange, routingkey, systemMessage);
        String responseText = systemMessage + " (RabbitMQ 처리 완료)";
        System.out.println(responseText);
        //최종 가공된 결과 반환
        return new StompAnalyzeResponse(
                responseText,
                answerList,
                getCurrentFormattedTime()
        );
    }

    //인사 메시지 응답
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public BotMessage greeting(ClientMessage message) throws Exception {
        Thread.sleep(50);
        String responseText = "안녕하세요, 챗봇(WebSocket)입니다.\n궁금한 점은 저에게 물어보세요.";
        return new BotMessage(responseText, getCurrentFormattedTime());
    }

    //일반 메시지 응답
    @MessageMapping("/message")
    @SendTo("/topic/message")
    public StompAnalyzeResponse message(StompAnalyzeRequest request){
        String content = (request.content() == null) ? "" : request.content().strip();
        //komoran 분석기로 명사 추출(message에 들어있는 것들 기준)
        KomoranResult analyzeResult = komoran.analyze(content);
        //추출한 명사들을 List로 저장
        List<String> nouns = analyzeResult.getNouns();

        String systemMessage = "질문을 분류하지 못했습니다.";
        //질문 리스트를 저장할 배열
        List<String> answerList = new ArrayList<>();

        //대주제 선점용 타겟 오브젝트(질문의 주제 등)
        ChatEntity matchedChat = null;

        //"서울 날씨" 처럼 대주제 키워드 포함 여부 DB 매칭
        for (String noun : nouns) {
            var chatOpt = chatRepository.findBySearch(noun);
            if (chatOpt.isPresent()) {
                matchedChat = chatOpt.get();
                break; // 가장 먼저 매칭된 대주제를 가지고 루프 탈출
            }
        }
        //비즈니스 검색 로직 분기
        if (matchedChat != null) {
            systemMessage = matchedChat.getResStr(); // 시스템메시지에 어떤걸 물어봤는지 저장
            boolean hasDetailMatch = false;

            //함께 추출된 명사들 중 세부 항목명('서울' 등)이 있는지 검색
            for (String noun : nouns) {
                // 대주제 검색 키워드 자체는 세부 항목이 아니므로 제외
                if (!noun.equals(matchedChat.getSearch())) {
                    List<AnswerEntity> details = answerRepository.findByChatEntityIdAndName(matchedChat.getId(), noun);
                    if (!details.isEmpty()) {
                        for (AnswerEntity answer : details) {
                            answerList.add("[" + answer.getName() + "] " + answer.getContent());
                        }
                        hasDetailMatch = true;
                    }
                }
            }

            //"날씨 알려줘" 처럼 세부 명사 매칭이 전혀 없는 경우 카테고리 전체 목록 로드
            if (!hasDetailMatch && matchedChat.getAnswerEntities() != null) {
                for (AnswerEntity answer : matchedChat.getAnswerEntities()) {
                    answerList.add("[" + answer.getName() + "] " + answer.getContent());
                }
            }

        } else {
            //추출된 명사 중 매칭되는 대주제가 전혀 없을 경우 '기타' 카테고리 로드
            var etcOpt = chatRepository.findBySearch("기타");
            if (etcOpt.isPresent()) {
                ChatEntity etcChat = etcOpt.get();
                systemMessage = etcChat.getResStr(); // "날씨,상품,운동 이외의 질문입니다."

                // '기타' 카테고리에 묶여있는 기본 답변이 있다면 함께 표출
                if (etcChat.getAnswerEntities() != null) {
                    for (AnswerEntity answer : etcChat.getAnswerEntities()) {
                        answerList.add(answer.getContent());
                    }
                }
            } else {
                systemMessage = "날씨,상품,운동 이외의 질문입니다.";
                answerList.add("제공할 수 있는 답변 카테고리가 존재하지 않습니다.");
            }
        }
        rabbitTemplate.convertAndSend(exchange, routingkey, systemMessage);
        String responseText = systemMessage + " 에 대한 응답입니다.";
        //최종 가공된 결과 반환
        return new StompAnalyzeResponse(
                responseText,
                answerList,
                getCurrentFormattedTime()
        );
    }

    //RabbitMQ 전송 + 챗봇 응답
//    @MessageMapping("/bot")
//    @SendTo("/topic/question")
//    public BotMessage rabbitChat(Question message) throws Exception {
//        rabbitTemplate.convertAndSend(exchange, routingkey, message);
//
//        String responseText = message.getContent() + " (RabbitMQ 처리 완료)";
//        return new BotMessage(responseText, getCurrentFormattedTime());
//    }

    //일반 메시지 응답
//    @MessageMapping("/message")
//    @SendTo("/topic/message")
//    public BotMessage message(ClientMessage message) throws Exception {
//        Thread.sleep(50);
//        String searchData = message.getContent().trim();
//        System.out.println("입력메시지 >> " + searchData);
//
//        String responseText = message.getContent() + "에 대한 응답입니다.";
//        return new BotMessage(responseText, getCurrentFormattedTime());
//    }

    //공통 시간 포맷 생성 메서드   
    private String getCurrentFormattedTime() {
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h:mm");
        return today.format(formatter);
    }
}
