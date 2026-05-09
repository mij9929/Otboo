package com.codeit.otboo.domain.clothes.recommendation.dto.internal;

import java.util.List;
import java.util.UUID;

public record LlmRecommendationResponse(
        List<UUID> selectedClothesIds,
        String reason
) {
}