package org.spring.backend.calendar.service;

import org.spring.backend.calendar.dto.PersonalScheduleDto;

import java.io.IOException;
import java.util.List;

public interface PersonalScheduleService {

    // 일정 등록
    void insertSchedule(Long memberId, PersonalScheduleDto personalScheduleDto)  throws IOException;

    // 일정 수정
    void updateSchedule(Long scheduleId, Long memberId, PersonalScheduleDto personalScheduleDto) throws IOException;

    // 일정 삭제
    void deleteSchedule(Long scheduleId, Long memberId) throws IOException;
}
