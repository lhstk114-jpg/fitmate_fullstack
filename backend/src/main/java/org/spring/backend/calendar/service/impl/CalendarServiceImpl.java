package org.spring.backend.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.calendar.repository.PersonalScheduleRepository;
import org.spring.backend.calendar.dto.CalendarDto;
import org.spring.backend.calendar.service.CalendarService;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.file.repository.FileRepository;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final MemberRepository memberRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final FileRepository fileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarDto> getCalendar(Long memberId, String eventType) {
        validateMember(memberId);
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType이 입력되지 않았습니다.");
        }

        return switch (eventType) {
            case "ALL" -> getAllCalendar(memberId);

            case "SUBSCRIPTION" -> getSubscription(memberId);

            case "PT" -> getPt(memberId);

            case "WORKOUT" -> getPersonalSchedule(memberId, "WORKOUT");

            case "PERSONAL" -> getPersonalSchedule(memberId, "PERSONAL");

            default -> throw new IllegalArgumentException("지원하지 않는 eventType입니다: " + eventType);
        };
    }

    private List<CalendarDto> getAllCalendar(Long memberId) {

        List<CalendarDto> calendarList = new ArrayList<>();

        calendarList.addAll(getSubscription(memberId));
        calendarList.addAll(getPt(memberId));
        calendarList.addAll(getPersonalSchedule(memberId, "WORKOUT"));
        calendarList.addAll(getPersonalSchedule(memberId, "PERSONAL"));

        return calendarList;
    }

    private List<CalendarDto> getPersonalSchedule(Long memberId,String eventType) {

        return personalScheduleRepository
                .findByMemberEntityIdAndEventType(
                        memberId,
                        eventType
                )
                .stream()
                .map(this::convertCalendarDto)
                .toList();
    }

    //schedule Entity -> Calendar Dto
    private CalendarDto convertCalendarDto(PersonalScheduleEntity personalScheduleEntity) {

        FileEntity fileEntity = fileRepository.findByPersonalScheduleEntity(personalScheduleEntity)
                .orElse(null);

        return CalendarDto.fromPersonalSchedule(personalScheduleEntity,fileEntity);
    }

    private List<CalendarDto> getSubscription(Long memberId) {
        // 추후 SubscriptionRepository 연결
        return new ArrayList<>();
    }

    private List<CalendarDto> getPt(Long memberId) {
        // 추후 PtRepository 연결
        return new ArrayList<>();
    }

    private void validateMember(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID가 입력되지 않았습니다.");
        }

        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }
    }
}