package org.spring.backend.admin.popup.entity;

import jakarta.persistence.*;
import lombok.*;

import org.spring.backend.admin.popup.dto.PopupDto;
import org.spring.backend.common.BasicTime;
import org.spring.backend.file.entity.FileEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "popup_tb")
public class PopupEntity extends BasicTime {

    @Id
    @Column(name = "popup_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private LocalDateTime startDate;

    // 노출 종료일
    private LocalDateTime endDate;

    //팝업 파일 목록
    @OneToMany(
            mappedBy = "popupEntity",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<FileEntity> fileEntities = new ArrayList<>();

    //팝업 등록용 Entity 변환
    public static PopupEntity toInsertPopupEntity(PopupDto popupDto) {
        return PopupEntity.builder()
                .title(popupDto.getTitle())
                .content(popupDto.getContent())
                .linkUrl(popupDto.getLinkUrl())
                .active(popupDto.getActive())
                .sortOrder(popupDto.getSortOrder())
                .startDate(popupDto.getStartDate())
                .endDate(popupDto.getEndDate())
                .build();
    }

    //수정값만 현재 엔티티에 반영
    public void toUpdatePopup(PopupDto popupDto) {
        this.title = popupDto.getTitle();
        this.content = popupDto.getContent();
        this.linkUrl = popupDto.getLinkUrl();
        this.active = popupDto.getActive();
        this.sortOrder = popupDto.getSortOrder();
        this.startDate = popupDto.getStartDate();
        this.endDate = popupDto.getEndDate();
    }
}