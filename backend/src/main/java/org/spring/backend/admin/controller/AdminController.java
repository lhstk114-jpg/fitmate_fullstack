package org.spring.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.community.dto.TabDto;
import org.spring.backend.community.service.TabService;
import org.spring.backend.shop.order.dto.OrderDto;
import org.spring.backend.shop.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TabService tabService;
    private final OrderService orderService;

    // =======================popup=======================

    // 탭생성
    @PostMapping("/tabInsert")
    public ResponseEntity<?> tabInsert(@RequestBody List<TabDto> tabDto) {
        tabService.insertTab(tabDto);
        Map<String, List<TabDto>> map = new HashMap<>();

        map.put("tab", tabDto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    // 탭 삭제
    @DeleteMapping("/tabDelete/{id}")
    public ResponseEntity<?> tabDelete(@PathVariable("id") Long id) {

        tabService.tabDelete(id);
        Map<String, String> map = new HashMap<>();
        map.put("result", "Delete");
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    // 탭 수정
    @PutMapping("/tabUpdate/{id}")
    public ResponseEntity<?> tabUpdate(@PathVariable("id") Long id, @RequestBody TabDto tabDto) {
        tabDto.setId(id);
        Map<String, TabDto> map = new HashMap<>();

        tabService.tabUpdate(tabDto);
        map.put("result", tabDto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    // 탭 상세 //
    @GetMapping("/tabDetail/{id}")
    public ResponseEntity<?> tabDetail(@PathVariable("id") Long id) {
        Map<String, TabDto> map = new HashMap<>();

        TabDto tabDto = tabService.tabDetail(id);
        map.put("tab", tabDto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

}
