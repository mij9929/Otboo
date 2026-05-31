package com.codeit.otboo.domain.clothes.recommendation.dto.internal;

import com.codeit.otboo.domain.profile.entity.Profile;

public record ProfileSummary(
        Integer temperatureSensitivity
) {
    public static ProfileSummary from(Profile profile) {
        return new ProfileSummary(
                profile.getTemperatureSensitivity()
        );
    }
}