package com.codeit.otboo.domain.clothes.recommendation.dto.internal;

import java.util.List;

public record LlmRecommendationRequest(
        WeatherSummary weather,
        ProfileSummary profile,
        List<OutfitCandidate> candidates
) {
    public static LlmRecommendationRequest from(
            RecommendationContext context,
            List<OutfitCandidate> candidates
    ) {
        return new LlmRecommendationRequest(
                WeatherSummary.from(context.weather()),
                ProfileSummary.from(context.profile()),
                candidates
        );
    }
}