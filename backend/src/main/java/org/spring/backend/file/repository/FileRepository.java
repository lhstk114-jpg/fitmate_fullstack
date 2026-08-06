package org.spring.backend.file.repository;

import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.admin.popup.entity.PopupEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByMemberEntity(MemberEntity memberEntity);

    Optional<FileEntity> findByCommunityEntity(CommunityEntity communityEntity);

    List<FileEntity> findByProductEntityOrderBySortOrderAsc(ProductEntity productEntity);

    Optional<FileEntity> findByPopupEntity(PopupEntity popupEntity);

    Optional<FileEntity> findByPersonalScheduleEntity(PersonalScheduleEntity personalScheduleEntity);

}
