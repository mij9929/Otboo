package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ClothesAttributeDefSseEvent extends BaseSseEvent {
    private final UUID targetId;
    private final NotificationType notificationType;
    public ClothesAttributeDefSseEvent(String title, String content, UUID targetId) {
        super(title, content);
        this.targetId = targetId;
        this.notificationType = NotificationType.CLOTHES_ATTRIBUTE;
    }
}
