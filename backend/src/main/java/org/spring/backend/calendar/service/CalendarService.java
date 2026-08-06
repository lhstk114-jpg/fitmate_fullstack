package org.spring.backend.calendar.service;

import org.spring.backend.calendar.dto.CalendarDto;

import java.util.List;

public interface CalendarService {
    // memberId와 eventType에 맞는 캘린더 일정 조회
    List<CalendarDto> getCalendar(Long memberId, String eventType);
}
