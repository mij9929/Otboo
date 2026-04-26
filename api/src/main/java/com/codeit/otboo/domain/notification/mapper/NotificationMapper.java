package com.codeit.otboo.domain.notification.mapper;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    // 응답용
    public NotificationResponse toDto(NotificationDto notificationDto) {
        return new NotificationResponse(
                notificationDto.id(),
                notificationDto.createdAt(),
                notificationDto.receiverId(),
                notificationDto.title(),
                notificationDto.content(),
                notificationDto.level(),
                notificationDto.notificationType(),
                notificationDto.targetId()
        );
    }

    // 내부 -> dto
    public NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .createdAt(notification.getCreatedAt())
                .receiverId(notification.getReceiver().getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .level(notification.getLevel())
                .notificationType(notification.getNotificationType())
                .targetId(notification.getTargetId())
                .build();
    }
}
