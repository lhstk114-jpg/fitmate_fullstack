package org.spring.backend.calendar.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.calendar.dto.PersonalScheduleDto;
import org.spring.backend.calendar.service.PersonalScheduleService;
import org.spring.backend.calendar.dto.CalendarDto;
import org.spring.backend.calendar.service.CalendarService;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final PersonalScheduleService personalScheduleService;
    private final MemberRepository memberRepository;

    // 일정 목록 조회
    @GetMapping("/scheduleList")
    public ResponseEntity<List<CalendarDto>> getScheduleList(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String eventType) {
        MemberEntity memberEntity = getLoginMember(user);

        List<CalendarDto> response =
                calendarService.getCalendar(memberEntity.getId(),eventType);

        return ResponseEntity.ok(response);
    }

    // 일정 등록
    @PostMapping(
            value = "/insert"
            ,consumes = "multipart/form-data"
    )
    public ResponseEntity<String> scheduleInsert(
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute PersonalScheduleDto personalScheduleDto) throws IOException {
        MemberEntity memberEntity = getLoginMember(user);

        personalScheduleService.insertSchedule(memberEntity.getId(),personalScheduleDto);

        return ResponseEntity.ok("ok");
    }

    // 일정 수정
    @PutMapping(
            value = "/update/{scheduleId}"
            ,consumes = "multipart/form-data"
    )
    public ResponseEntity<String> scheduleUpdate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId,
            @ModelAttribute PersonalScheduleDto personalScheduleDto) throws IOException {
        MemberEntity memberEntity = getLoginMember(user);

        personalScheduleService.updateSchedule(scheduleId,memberEntity.getId(),personalScheduleDto);

        return ResponseEntity.ok("ok");
    }

    // 일정 삭제
    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<String> scheduleDelete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId) throws IOException {
        MemberEntity memberEntity = getLoginMember(user);

        personalScheduleService.deleteSchedule(scheduleId,memberEntity.getId());

        return ResponseEntity.ok("ok");
    }

    // 로그인 회원 조회
    private MemberEntity getLoginMember(CustomUserDetails user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByUserEmail(user.getUsername())
                .orElseThrow(() ->new NoSuchElementException("회원 정보를 불러올 수 없습니다."));
    }
}