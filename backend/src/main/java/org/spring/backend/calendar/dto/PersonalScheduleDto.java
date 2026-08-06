package org.spring.backend.calendar.dto;

import lombok.*;
import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.file.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalScheduleDto {
    // 캘린더 이벤트 식별용
    private Long id;

    // 원본 데이터 식별용
    private Long sourceId;

    // SUBSCRIPTION, PT, WORKOUT, PERSONAL
    private String eventType;

    private String title;

    private String description;

    private LocalDateTime start;

    private LocalDateTime end;

    // 사용자가 수정할 수 있는 일정인지
    private Boolean editable;

    // 등록 또는 수정 시 새로 업로드한 실제 파일
    private MultipartFile attachFile;

    // 기존 파일의 원본 파일명
    private String oldFileName;

    // 기존 파일의 서버 저장 파일명
    private String newFileName;

    //Entity -> Dto 파일 포함
    public static PersonalScheduleDto toPersonalScheduleDto(PersonalScheduleEntity entity,FileEntity fileEntity){
        return PersonalScheduleDto.builder()
                .id(entity.getId())
                .sourceId(entity.getId())
                .eventType(entity.getEventType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .start(entity.getStart())
                .end(entity.getEnd())
                //WorkOut, Personal 은 수정 가능
                .editable(true)
                //파일 정보는 FileEntity에서 조회
                .oldFileName(fileEntity != null ? fileEntity.getOldFileName() : null)
                .newFileName(fileEntity != null ? fileEntity.getNewFileName() : null)
                .build();
    }
}
