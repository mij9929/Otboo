package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.OutfitCandidate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("fake-llm")
public class FakeLlmRecommendationClient implements LlmRecommendationClient {

    @Override
    public LlmRecommendationResponse recommend(LlmRecommendationRequest request) {
        return new LlmRecommendationResponse(
                request.candidates().stream()
                        .limit(3)
                        .map(OutfitCandidate::clothesId)
                        .toList(),
                "임시 LLM 응답입니다."
        );
    }
}