package org.spring.backend.chatbot.dto;

import java.util.List;

public record StompAnalyzeResponse(
String responseText , //답변 메세지(기본 봇 메세지)
List<String> answerList, //답변 목록 데이터
String time  //시간 데이터
) {
}
