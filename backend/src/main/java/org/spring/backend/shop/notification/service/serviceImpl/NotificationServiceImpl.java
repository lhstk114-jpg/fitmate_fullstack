package org.spring.backend.shop.notification.service.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.management.Notification;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.notification.dto.NotificationDto;
import org.spring.backend.shop.notification.entity.NotificationEntity;
import org.spring.backend.shop.notification.repository.NotificationRepository;
import org.spring.backend.shop.notification.service.NotificationService;
import org.spring.backend.shop.subscription.entity.SubscriptionEntity;
import org.spring.backend.shop.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final MemberRepository memberRepository;

  @Override
  public void insertNotification(Long memberId, NotificationDto notificationDto) {
    
    MemberEntity memberEntity = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("member 없음"));

    SubscriptionEntity subscription = null;

    if (notificationDto.getSubscriptionId() != null) {
      subscription = subscriptionRepository.findById(notificationDto.getSubscriptionId())
          .orElse(null);
    }

    NotificationEntity notificationEntity = NotificationEntity.builder()
        .id(notificationDto.getId())
        .title(notificationDto.getTitle())
        .content(notificationDto.getContent())
        .isRead(0)
        .subscriptionEntity(subscription)
        .build();

    notificationRepository.save(notificationEntity);
  }

  @Override
  public List<NotificationDto> notificationList(Long memberId) {
    return notificationRepository.findByMemberEntity_IdOrderByIdDesc(memberId)
        .stream()
        .map(NotificationDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public int unreadCount(Long memberId) {
    return notificationRepository.countByMemberEntity_IdAndIsRead(memberId, 0);
  }

  @Override
  public void readNotification(Long notificationId) {

    NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new IllegalArgumentException("알림 없음"));

        notificationEntity.setIsRead(1);
  }

  @Override
  public void readAllNotification(Long memberId) {
    List<NotificationEntity> list =
                notificationRepository.findByMemberEntity_Id(memberId);

        list.forEach(n -> n.setIsRead(1));
  }

  @Override
  public void deleteNotification(Long notificationId) {
    notificationRepository.deleteById(notificationId);
  }

}
