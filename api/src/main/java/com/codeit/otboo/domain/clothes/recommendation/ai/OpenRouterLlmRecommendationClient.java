package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.ai.dto.OpenRouterChatResponse;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.exception.InvalidLlmRecommendationResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenRouterLlmRecommendationClient implements LlmRecommendationClient{
    @Override
    public LlmRecommendationResponse recommend(LlmRecommendationRequest request) {
        return null;
    }

    private String extractContent(OpenRouterChatResponse response) {
        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null
                || response.choices().get(0).message().content().isBlank()) {
            throw new InvalidLlmRecommendationResponseException(
                    "LLM 응답 content가 비어 있습니다."
            );
        }

        return response.choices().get(0).message().content();
    }
}
