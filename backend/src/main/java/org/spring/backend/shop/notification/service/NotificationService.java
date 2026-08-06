package org.spring.backend.shop.notification.service;

import java.util.List;

import org.spring.backend.shop.notification.dto.NotificationDto;

public interface NotificationService {
      // 알림 생성
    void insertNotification(Long memberId, NotificationDto notificationDto);

    // 알림 목록 조회
    List<NotificationDto> notificationList(Long memberId);

    // 안 읽은 알림 개수
    int unreadCount(Long memberId);

    // 알림 읽음 처리
    void readNotification(Long notificationId);

    // 전체 읽음 처리
    void readAllNotification(Long memberId);

    // 알림 삭제
    void deleteNotification(Long notificationId);
}
