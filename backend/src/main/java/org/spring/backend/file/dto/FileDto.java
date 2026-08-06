package org.spring.backend.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.admin.popup.entity.PopupEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.type.ImageType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
    private Long id;

    private TableType tableType;

    private String newFileName;

    private String oldFileName;

    private String category;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 각자 매핑했던 id, entity 불러오는용도
    private Long memberId;

    private Long communityId;

    private Long productId;

    private Long popupId;

    private MemberEntity memberEntity;

    private CommunityEntity communityEntity;

    private ProductEntity productEntity;

    private PopupEntity popupEntity;

    private ImageType imageType;

    private int sortOrder;

    public static FileDto toFileDto(FileEntity fileEntity) {

        return FileDto.builder()
                .id(fileEntity.getId())
                .tableType(fileEntity.getTableType())
                .newFileName(fileEntity.getNewFileName())
                .oldFileName(fileEntity.getOldFileName())
                .imageType(fileEntity.getImageType())
                .sortOrder(fileEntity.getSortOrder())
                .createTime(fileEntity.getCreateTime())
                .updateTime(fileEntity.getUpdateTime())
                .build();
    }
}
