package com.codeit.otboo.domain.notification.dto;

public enum NotificationType {
    DIRECT_MESSAGE(true),
    FEED_LIKE(true),
    FEED_COMMENT(true),
    FEED_CREATE(true),
    FOLLOW(true),
    CLOTHES_ATTRIBUTE(false),
    ROLE_CHANGED(false),
    WEATHER_ALERT(false);

    private final boolean requiresTarget;

    NotificationType(boolean requiresTarget) {
        this.requiresTarget = requiresTarget;
    }

    public boolean requiresTarget() {
        return requiresTarget;
    }
}
