package org.spring.backend.chatbot.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.chatbot.dto.AnswerDto;
import org.spring.backend.chatbot.dto.ChatDto;
import org.spring.backend.chatbot.service.ChatBotAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatBotAdminController {
    private final ChatBotAdminService chatBotService;

    @GetMapping("/list/chat")
    public ResponseEntity<?> listChat(@PageableDefault(page = 0, size = 5, sort="id",
                                                  direction = Sort.Direction.ASC) Pageable pageable,
                                      @RequestParam(value = "subject",required = false)String subject,
                                      @RequestParam(value = "search", required = false)String search){
        Page<ChatDto> chatList = chatBotService.getChatList(pageable,subject,search);
        int newPage = chatList.getNumber(); //현재페이지
        int totalPage = chatList.getTotalPages(); //전체페이지
        int blockNum = 5; //한페이지에 보여질 페이지넘버의 수

        //블록 시작
        int startPage = (newPage / blockNum) * blockNum + 1; //시작페이지
        //블록 끝
        int endPage = Math.min(startPage+blockNum-1, totalPage); //끝페이지
        Map<String, Object> response = new HashMap<>();
        response.put("chatList", chatList.getContent());
        response.put("currentPage", newPage);
        response.put("totalPage", totalPage);
        response.put("startPage", startPage);
        response.put("totalElements", chatList.getTotalElements());
        response.put("endPage", endPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/list/answer/{id}")
    public ResponseEntity<?> listChat(@PageableDefault(page = 0, size = 5, sort="id",
                                                  direction = Sort.Direction.ASC) Pageable pageable,
                                      @RequestParam(value = "subject",required = false)String subject,
                                      @RequestParam(value = "search", required = false)String search,
                                      @PathVariable("id")Long id){
        Page<AnswerDto> answerList = chatBotService.getAnswerListByChatId(id,pageable,subject,search);
        int newPage = answerList.getNumber(); //현재페이지
        int totalPage = answerList.getTotalPages(); //전체페이지
        int blockNum = 5; //한페이지에 보여질 페이지넘버의 수

        //블록 시작
        int startPage = (newPage / blockNum) * blockNum + 1; //시작페이지
        //블록 끝
        int endPage = Math.min(startPage+blockNum-1, totalPage); //끝페이지
        Map<String, Object> response = new HashMap<>();
        response.put("chatList", answerList.getContent());
        response.put("currentPage", newPage);
        response.put("totalPage", totalPage);
        response.put("startPage", startPage);
        response.put("totalElements", answerList.getTotalElements());
        response.put("endPage", endPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/insert/chat")
    public ResponseEntity<?> insertChat(ChatDto chatDto){
        chatBotService.insertChat(chatDto);

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/insert/answer/{id}")
    public ResponseEntity<?> insertAnswer(AnswerDto answerDto, @PathVariable("id") Long chatId){
        chatBotService.insertAnswer(answerDto, chatId);

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/update/chat")
    public ResponseEntity<?> updateChat(ChatDto chatDto){
        chatBotService.updateChat(chatDto);

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/update/answer/{id}")
    public ResponseEntity<?> updateAnswer(AnswerDto answerDto, @PathVariable("id") Long chatId){
        chatBotService.updateAnswer(answerDto, chatId);

        return ResponseEntity.ok("ok");
    }
    @GetMapping("/detail/chat/{id}")
    public ResponseEntity<?> updateChat(@PathVariable("id")Long id){
        ChatDto chatDto = chatBotService.detailChat(id);

        Map<String, ChatDto> map = new HashMap<>();
        map.put("result", chatDto);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/detail/answer/{id}")
    public ResponseEntity<?> updateAnswer(@PathVariable("id")Long id){
        AnswerDto answerDto = chatBotService.detailAnswer(id);

        Map<String, AnswerDto> map = new HashMap<>();
        map.put("result", answerDto);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    @DeleteMapping("/delete/chat/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable("id")Long id){
        chatBotService.deleteChat(id);

        return ResponseEntity.ok("ok");
    }

    @DeleteMapping("/delete/answer/{id}")
    public ResponseEntity<?> deleteAnswer(@PathVariable("id")Long id){
        chatBotService.deleteAnswer(id);

        return ResponseEntity.ok("ok");
    }

}
