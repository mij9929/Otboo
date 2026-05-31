package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;

public interface LlmRecommendationClient {
    LlmRecommendationResponse recommend(LlmRecommendationRequest request);
}
