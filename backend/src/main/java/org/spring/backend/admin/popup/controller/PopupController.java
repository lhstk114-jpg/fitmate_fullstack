package org.spring.backend.admin.popup.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.admin.popup.dto.PopupDto;
import org.spring.backend.admin.popup.service.PopupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PopupController {
    private final PopupService popupService;

    //=======================popup=======================
// 팝업 목록
//    @GetMapping("/popupList")
//    public ResponseEntity<?> popupList() {
//        Map<String, List<PopupDto>> map = new HashMap<>();
//
//        List<PopupDto> popupList = mainService.popupList();
//        map.put("result", popupList);
//
//        return ResponseEntity.ok(map);
//    }
    @GetMapping("/popupList")
    public ResponseEntity<?> popupList(@PageableDefault(page = 0, size = 5, sort="id",
                                               direction = Sort.Direction.ASC) Pageable pageable,
                                       @RequestParam(value = "subject",required = false)String subject,
                                       @RequestParam(value = "search", required = false)String search){
        Page<PopupDto> popupList = popupService.popupList(pageable, subject, search);

        int newPage = popupList.getNumber(); //현재페이지
        int totalPage = popupList.getTotalPages(); //전체페이지
        int blockNum = 5; //한페이지에 보여질 페이지넘버의 수

        //블록 시작
        int startPage = (newPage / blockNum) * blockNum + 1; //시작페이지
        //블록 끝
        int endPage = Math.min(startPage+blockNum-1, totalPage); //끝페이지
        Map<String, Object> response = new HashMap<>();
        response.put("popupList", popupList.getContent());
        response.put("currentPage", newPage);
        response.put("totalPage", totalPage);
        response.put("startPage", startPage);
        response.put("totalElements", popupList.getTotalElements());
        response.put("endPage", endPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // 팝업 등록
    @PostMapping(value = "/popupInsert",
            consumes = "multipart/form-data")
    public ResponseEntity<?> popupInsert(@ModelAttribute PopupDto popupDto) throws IOException {
        popupService.insertPopup(popupDto);

        return ResponseEntity.ok("ok");
    }

    // 팝업 삭제
    @DeleteMapping("/popupDelete/{popupId}")
    public ResponseEntity<?> popupDelete(@PathVariable Long popupId) throws IOException {
        popupService.deletePopup(popupId);

        return ResponseEntity.ok("ok");
    }

    // 팝업 수정
    @PutMapping(value = "/popupUpdate/{popupId}",
            consumes = "multipart/form-data")
    public ResponseEntity<?> popupUpdate(@PathVariable Long popupId,@ModelAttribute PopupDto popupDto) throws IOException {
        popupDto.setId(popupId);
        popupService.updatePopup(popupDto);

        return ResponseEntity.ok("ok");
    }
}
