package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class FeedLikedEvent extends BaseSseEvent {
    private final UUID receiverId;
    private final UUID targetId;
    private final NotificationType notificationType;

    public FeedLikedEvent(String title, String content, UUID receiverId, UUID targetId) {
        super(title, content);
        this.receiverId = receiverId;
        this.targetId = targetId;
        this.notificationType = NotificationType.FEED_LIKE;
    }
}
