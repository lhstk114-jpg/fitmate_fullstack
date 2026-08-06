package org.spring.backend.admin.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.admin.dashboard.dto.DashboardResponseDto;
import org.spring.backend.admin.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    // 관리자 대시보드 전체 조회
    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard() {

        DashboardResponseDto response =
                dashboardService.getDashboardData();

        return ResponseEntity.ok(response);
    }
}