package org.spring.backend.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.spring.backend.calendar.dto.PersonalScheduleDto;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.member.entity.MemberEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "personal_schedule_tb")
public class PersonalScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_schedule_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    // WORKOUT, PERSONAL
    @Column(nullable = false)
    private String eventType;

    // 일정 시작 시간
    @Column(nullable = false)
    private LocalDateTime start;

    // 일정 종료 시간
    @Column(nullable = false)
    private LocalDateTime end;

    // 일정을 등록한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity memberEntity;

    // 일정 첨부 파일 목록
    @Builder.Default
    @OneToMany(
            mappedBy = "personalScheduleEntity",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<FileEntity> fileEntities = new ArrayList<>();

    // DTO -> Entity 등록용
    public static PersonalScheduleEntity toInsertPersonalScheduleEntity(PersonalScheduleDto dto,MemberEntity memberEntity) {
        return PersonalScheduleEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventType(dto.getEventType())
                .start(dto.getStart())
                .end(dto.getEnd())
                .memberEntity(memberEntity)
                .build();
    }

    // 일정 수정용
    public void toUpdate(PersonalScheduleDto dto) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.eventType = dto.getEventType();
        this.start = dto.getStart();
        this.end = dto.getEnd();
    }
}