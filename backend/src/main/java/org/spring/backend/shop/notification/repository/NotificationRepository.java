package org.spring.backend.shop.notification.repository;

import java.util.List;

import org.spring.backend.shop.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  List<NotificationEntity> findByMemberEntity_IdOrderByIdDesc(Long memberId);

  List<NotificationEntity> findByMemberEntity_Id(Long memberId);

  int countByMemberEntity_IdAndIsRead(Long memberId, int isRead);
}
