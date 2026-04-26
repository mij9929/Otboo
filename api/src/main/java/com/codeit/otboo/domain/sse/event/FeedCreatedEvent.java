package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationType;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class FeedCreatedEvent extends BaseSseEvent{
    private final List<UUID> receiverIds;
    private final UUID targetId;
    private final NotificationType notificationType;


    public FeedCreatedEvent(String title, String content, List<UUID> receiverIds, UUID targetId) {
        super(title, content);
        this.receiverIds = receiverIds;
        this.targetId = targetId;
        this.notificationType = NotificationType.FEED_CREATE;
    }
}