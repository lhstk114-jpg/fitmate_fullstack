package org.spring.backend.admin.popup.dto;

import lombok.*;
import org.spring.backend.admin.popup.entity.PopupEntity;
import org.spring.backend.file.entity.FileEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopupDto {

    private Long id;

    // 팝업 제목
    private String title;

    // 팝업 내용
    private String content;

    // 클릭 시 이동할 주소
    private String linkUrl;

    // 노출 여부
    private Boolean active;

    // 노출 순서
    private Integer sortOrder;

    // 노출 시작일
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    // 노출 종료일
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    // 등록 또는 수정 시 새로 업로드한 실제 파일
    private MultipartFile attachFile;

    // 기존 파일의 원본 파일명
    private String oldFileName;

    // 기존 파일의 서버 저장 파일명
    private String newFileName;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Entity -> Dto 파일포함
     public static PopupDto toPopupDto(PopupEntity popupEntity, FileEntity fileEntity) {
        return PopupDto.builder()
                .id(popupEntity.getId())
                .title(popupEntity.getTitle())
                .content(popupEntity.getContent())
                .linkUrl(popupEntity.getLinkUrl())
                .active(popupEntity.getActive())
                .sortOrder(popupEntity.getSortOrder())
                .startDate(popupEntity.getStartDate())
                .endDate(popupEntity.getEndDate())
                // 파일 정보는 FileEntity에서 조회
                .oldFileName(
                        fileEntity != null
                                ? fileEntity.getOldFileName()
                                : null
                )
                .newFileName(
                        fileEntity != null
                                ? fileEntity.getNewFileName()
                                : null
                )
                .createTime(popupEntity.getCreateTime())
                .updateTime(popupEntity.getUpdateTime())
                .build();
    }
}