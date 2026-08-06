package org.spring.backend.community.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.community.dto.CommunityReplyDto;
import org.spring.backend.community.service.CommunityReplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 게시글 댓글 CRUD 컨트롤러
 * 프론트: ReplyForm.jsx(작성), ReplyList.jsx(조회/수정/삭제)에서 사용
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reply")
public class CommunityReplyController {
    private final CommunityReplyService communityReplyService;

    /**
     * 댓글 작성
     * 프론트: ReplyForm.jsx의 saveReply → POST /reply/insert
     * QNA 카테고리 게시글은 관리자만 작성 가능한 정책이 있는데, 그 검증은 서비스단(communityReplyService)에서
     * 처리하는 것으로 추정 (프론트도 403 응답을 별도 처리하는 걸 보면 서버측 검증이 있는 것으로 보임)
     */
    //댓글 작성
    @PostMapping("/insert")
    public ResponseEntity<?> save(@RequestBody CommunityReplyDto dto){
        communityReplyService.insertReply(dto);
        Map<String, CommunityReplyDto> map = new HashMap<>();
        map.put("result", dto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     * 프론트: ReplyList.jsx의 getReplyList → GET /reply/list/{communityId}?count=false
     */
    //댓글 목록 조회
    @GetMapping("/list/{communityId}")
    public ResponseEntity<?> list(@PathVariable("communityId") Long communityId){
        Map<String, List<CommunityReplyDto>> map = new HashMap<>();
        List<CommunityReplyDto> replyList = communityReplyService.replyList(communityId);
        map.put("result", replyList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    // 댓글 단건 상세 조회 (현재 프론트에서 직접 호출하는 곳은 확인되지 않음)
    //댓글 상세 조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> detail(@PathVariable("id")Long id){
        Map<String, CommunityReplyDto> map = new HashMap<>();
        CommunityReplyDto detail = communityReplyService.detailReply(id);
        map.put("result", detail);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


    /**
     * 댓글 삭제
     * 프론트: ReplyList.jsx의 deleteEdit → DELETE /reply/delete/{id}
     * 작성자 본인/관리자 여부 판단은 프론트에서만 하고 있어, 서버측에서도 별도 권한 검증이 있는지 확인 필요
     */
    //댓글 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id){
        Map<String , String> map = new HashMap<>();
        communityReplyService.deleteReply(id);
        map.put("result", "Delete");
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    /**
     * 댓글 수정
     * 프론트: ReplyList.jsx의 saveEdit → PUT /reply/update/{id}
     */
    //댓글 수정
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody CommunityReplyDto dto){
        Map<String, CommunityReplyDto> map = new HashMap<>();
        communityReplyService.updateReply(id, dto);
        map.put("result", dto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

}
