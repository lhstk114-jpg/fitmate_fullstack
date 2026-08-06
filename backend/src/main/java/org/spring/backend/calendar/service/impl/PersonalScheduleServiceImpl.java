package org.spring.backend.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import org.spring.backend.calendar.dto.PersonalScheduleDto;
import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.calendar.repository.PersonalScheduleRepository;
import org.spring.backend.calendar.service.PersonalScheduleService;
import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.file.handler.FileHandler;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PersonalScheduleServiceImpl
        implements PersonalScheduleService {

    private final PersonalScheduleRepository personalScheduleRepository;
    private final MemberRepository memberRepository;
    private final FileHandler fileHandler;

    // 스케줄 이미지 저장 경로
    @Value("${img.path.schedule}")
    private String schedulePath;

    // 일정 등록
    @Transactional
    @Override
    public void insertSchedule(
            Long memberId,
            PersonalScheduleDto personalScheduleDto
    ) throws IOException {

        // 등록 가능한 eventType인지 확인
        validateEventType(
                personalScheduleDto.getEventType()
        );

        // 시작일과 종료일 확인
        validateScheduleTime(
                personalScheduleDto
        );

        // 일정 등록 회원 조회
        MemberEntity memberEntity =
                memberRepository.findById(memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "회원 정보가 없습니다."
                                )
                        );

        // DTO를 일정 Entity로 변환
        PersonalScheduleEntity personalScheduleEntity =
                PersonalScheduleEntity
                        .toInsertPersonalScheduleEntity(
                                personalScheduleDto,
                                memberEntity
                        );

        // 일정 저장 후 생성된 일정 ID 확보
        PersonalScheduleEntity savedSchedule =
                personalScheduleRepository.save(
                        personalScheduleEntity
                );

        // 첨부파일이 있는 경우에만 파일 저장
        if (hasAttachFile(personalScheduleDto)) {
            fileHandler.insertFile(
                    schedulePath,
                    TableType.SCHEDULE,
                    savedSchedule.getId(),
                    personalScheduleDto.getAttachFile()
            );
        }
    }

    // 일정 수정
    @Transactional
    @Override
    public void updateSchedule(
            Long scheduleId,
            Long memberId,
            PersonalScheduleDto personalScheduleDto
    ) throws IOException {

        // 수정 가능한 eventType인지 확인
        validateEventType(
                personalScheduleDto.getEventType()
        );

        // 시작일과 종료일 확인
        validateScheduleTime(
                personalScheduleDto
        );

        // 수정할 일정 조회
        PersonalScheduleEntity personalScheduleEntity =
                personalScheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "일정 정보가 없습니다."
                                )
                        );

        // 본인이 등록한 일정인지 확인
        validateScheduleOwner(
                personalScheduleEntity,
                memberId,
                "수정"
        );

        // 일정 정보 수정
        personalScheduleEntity.toUpdate(
                personalScheduleDto
        );

        //새 파일이 선택된 경우에만 파일 교체
        if (hasAttachFile(personalScheduleDto)) {
            fileHandler.insertFile(
                    schedulePath,
                    TableType.SCHEDULE,
                    scheduleId,
                    personalScheduleDto.getAttachFile()
            );
        }
    }

    // 일정 삭제
    @Transactional
    @Override
    public void deleteSchedule(
            Long scheduleId,
            Long memberId
    ) throws IOException {

        // 삭제할 일정 조회
        PersonalScheduleEntity personalScheduleEntity =
                personalScheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "일정 정보가 없습니다."
                                )
                        );

        // 본인이 등록한 일정인지 확인
        validateScheduleOwner(
                personalScheduleEntity,
                memberId,
                "삭제"
        );

        // 실제 파일과 FileEntity 삭제
        fileHandler.deleteFile(
                schedulePath,
                TableType.SCHEDULE,
                scheduleId
        );

        // 일정 Entity 삭제
        personalScheduleRepository.delete(
                personalScheduleEntity
        );
    }

    // 실제 첨부파일 존재 여부 확인
    private boolean hasAttachFile(
            PersonalScheduleDto personalScheduleDto
    ) {
        return personalScheduleDto.getAttachFile() != null
                && !personalScheduleDto
                .getAttachFile()
                .isEmpty();
    }

    // 본인이 등록한 일정인지 검증
    private void validateScheduleOwner(
            PersonalScheduleEntity personalScheduleEntity,
            Long memberId,
            String action
    ) {
        if (!personalScheduleEntity
                .getMemberEntity()
                .getId()
                .equals(memberId)) {

            throw new IllegalArgumentException(
                    "일정을 " + action
                            + "할 권한이 없습니다."
            );
        }
    }

    // 등록 또는 수정 가능한 일정 유형 검증
    private void validateEventType(
            String eventType
    ) {
        if (eventType == null
                || eventType.isBlank()) {

            throw new IllegalArgumentException(
                    "일정 유형은 필수입니다."
            );
        }

        if (!"WORKOUT".equalsIgnoreCase(eventType)
                && !"PERSONAL".equalsIgnoreCase(eventType)) {

            throw new IllegalArgumentException(
                    "직접 등록 가능한 일정 유형은 "
                            + "WORKOUT 또는 PERSONAL입니다."
            );
        }
    }

    // 일정 시작일과 종료일 검증
    private void validateScheduleTime(
            PersonalScheduleDto personalScheduleDto
    ) {
        if (personalScheduleDto.getStart() == null
                || personalScheduleDto.getEnd() == null) {

            throw new IllegalArgumentException(
                    "일정 시작일과 종료일은 필수입니다."
            );
        }

        /*종료일은 시작일보다 반드시 늦어야 함
          isBefore만 사용하면 시작과 종료가 같은 시간을
          허용하게 되므로 isAfter 기준으로 검증*/
        if (!personalScheduleDto.getEnd()
                .isAfter(
                        personalScheduleDto.getStart()
                )) {

            throw new IllegalArgumentException(
                    "일정 종료일은 시작일보다 늦어야 합니다."
            );
        }
    }
}