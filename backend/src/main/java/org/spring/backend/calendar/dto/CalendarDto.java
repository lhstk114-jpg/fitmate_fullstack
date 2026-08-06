package org.spring.backend.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.file.entity.FileEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDto {

    // 캘린더 이벤트 ID
    private Long id;

    // 실제 원본 데이터 ID
    // 수정, 삭제, 상세 조회 시 사용
    private Long sourceId;

    // 일정 유형
    // SUBSCRIPTION / PT / WORKOUT / PERSONAL
    private String eventType;

    // 일정 제목
    private String title;

    // 일정 시작일
    private LocalDateTime start;

    // 일정 종료일
    private LocalDateTime end;

    // 일정 상세 내용
    private String description;

    // 수정 가능 여부
    // 사용자가 직접 등록한 WORKOUT, PERSONAL은 true
    // 구독, PT 일정은 false
    private Boolean editable;

    // 업로드 당시 원본 파일명
    private String oldFileName;

    // 서버에 저장된 파일명
    private String newFileName;


    // PersonalScheduleEntity를 CalendarDto로 변환
    public static CalendarDto fromPersonalSchedule(PersonalScheduleEntity personalScheduleEntity,FileEntity fileEntity) {
        return CalendarDto.builder()
                .id(personalScheduleEntity.getId())
                .sourceId(personalScheduleEntity.getId())
                .eventType(personalScheduleEntity.getEventType())
                .title(personalScheduleEntity.getTitle())
                .start(personalScheduleEntity.getStart())
                .end(personalScheduleEntity.getEnd())
                .description(personalScheduleEntity.getDescription())
                .editable(true)

                .oldFileName(fileEntity != null ? fileEntity.getOldFileName() : null)
                .newFileName(fileEntity != null ? fileEntity.getNewFileName() : null)
                .build();
    }
}