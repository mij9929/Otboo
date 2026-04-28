package com.codeit.otboo.domain.notification.entity;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.dto.NotificationType;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private NotificationLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receiver_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_notifications_receivers",
                    foreignKeyDefinition = "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE"
            )
    )
    private User receiver;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "target_id")
    private UUID targetId;

    @Builder
    public Notification(String title, String content, NotificationLevel level, User receiver, NotificationType notificationType, UUID targetId) {
        validateTarget(notificationType, targetId);
        this.title = title;
        this.content = content;
        this.level = level;
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.targetId = targetId;
    }

    private static void validateTarget(NotificationType notificationType, UUID targetId) {
        if (notificationType == null) {
            throw new IllegalArgumentException("notificationType is required");
        }

        if (notificationType.requiresTarget() && targetId == null) {
            throw new IllegalArgumentException("targetId is required");
        }
    }
}
