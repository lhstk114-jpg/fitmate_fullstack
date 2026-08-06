package org.spring.backend.admin.dashboard.service;

import org.spring.backend.admin.dashboard.dto.DashboardResponseDto;

public interface DashboardService {
        // 관리자 대시보드 전체 데이터 조회
        DashboardResponseDto getDashboardData();

    }
