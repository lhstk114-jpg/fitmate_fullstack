package org.spring.backend.shop.notification.controller;

import java.util.List;

import org.spring.backend.shop.notification.dto.NotificationDto;
import org.spring.backend.shop.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {
  private final NotificationService notificationService;

  // 알림 생성
  @PostMapping("/{memberId}")
  public ResponseEntity<Void> insertNotification(
      @PathVariable Long memberId,
      @RequestBody NotificationDto notificationDto) {

    notificationService.insertNotification(memberId, notificationDto);
    return ResponseEntity.ok().build();
  }

  // 알림 목록 조회
  @GetMapping("/{memberId}")
  public ResponseEntity<List<NotificationDto>> notificationList(
      @PathVariable Long memberId) {

    return ResponseEntity.ok(notificationService.notificationList(memberId));
  }

  // 안 읽은 알림 개수
  @GetMapping("/{memberId}/unread-count")
  public ResponseEntity<Integer> unreadCount(
      @PathVariable Long memberId) {

    return ResponseEntity.ok(notificationService.unreadCount(memberId));
  }

  // 알림 읽음 처리
  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<Void> readNotification(
      @PathVariable Long notificationId) {

    notificationService.readNotification(notificationId);
    return ResponseEntity.ok().build();
  }

  // 전체 읽음 처리
  @PatchMapping("/{memberId}/read-all")
  public ResponseEntity<Void> readAllNotification(
      @PathVariable Long memberId) {

    notificationService.readAllNotification(memberId);
    return ResponseEntity.ok().build();
  }

  // 알림 삭제
  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> deleteNotification(
      @PathVariable Long notificationId) {

    notificationService.deleteNotification(notificationId);
    return ResponseEntity.ok().build();
  }
}
