package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.ai.dto.OpenRouterChatRequest;
import com.codeit.otboo.domain.clothes.recommendation.ai.dto.OpenRouterChatResponse;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.exception.InvalidLlmRecommendationResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenRouterLlmRecommendationClient implements LlmRecommendationClient{
    private final WebClient.Builder webClientBuilder;
    private final LlmRecommendationProperties properties;
    private final LlmRecommendationPromptBuilder promptBuilder;
    private final LlmRecommendationResponseParser responseParser;


    @Override
    public LlmRecommendationResponse recommend(LlmRecommendationRequest request) {
        String prompt = promptBuilder.build(request);

        OpenRouterChatRequest chatRequest =
                OpenRouterChatRequest.of(properties.model(), prompt);

        OpenRouterChatResponse chatResponse = webClientBuilder
                .baseUrl(properties.baseUrl())
                .build()
                .post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .bodyValue(chatRequest)
                .retrieve()
                .bodyToMono(OpenRouterChatResponse.class)
                .block();

        String content = extractContent(chatResponse);

        return responseParser.parse(content);

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
