package com.codeit.otboo.domain.sse.event;

import java.util.UUID;

import com.codeit.otboo.domain.notification.dto.NotificationType;
import lombok.Getter;

@Getter
public class FollowSseEvent extends BaseSseEvent {
    private final UUID receiverId;
    private final UUID targetId;
    private final NotificationType notificationType;

    public FollowSseEvent(String title, String content, UUID receiverId, UUID targetId) {
        super(title, content);
        this.receiverId = receiverId;
        this.targetId = targetId;
        this.notificationType = NotificationType.FOLLOW;
    }
}
