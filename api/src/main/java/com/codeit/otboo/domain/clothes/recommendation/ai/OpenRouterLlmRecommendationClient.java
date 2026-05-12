package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenRouterLlmRecommendationClient implements LlmRecommendationClient{
    @Override
    public LlmRecommendationResponse recommend(LlmRecommendationRequest request) {
        return null;
    }
}
