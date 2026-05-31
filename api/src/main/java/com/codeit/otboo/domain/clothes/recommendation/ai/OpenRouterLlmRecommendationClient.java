package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.ai.dto.OpenRouterChatRequest;
import com.codeit.otboo.domain.clothes.recommendation.ai.dto.OpenRouterChatResponse;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.exception.InvalidLlmRecommendationResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenRouterLlmRecommendationClient implements LlmRecommendationClient{
    private final WebClient llmWebClient;
    private final LlmRecommendationProperties properties;
    private final LlmRecommendationPromptBuilder promptBuilder;
    private final LlmRecommendationResponseParser responseParser;


    @Override
    public LlmRecommendationResponse recommend(LlmRecommendationRequest request) {
        String prompt = promptBuilder.build(request);

        OpenRouterChatRequest chatRequest =
                OpenRouterChatRequest.of(properties.model(), prompt);

        OpenRouterChatResponse chatResponse = llmWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(chatRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    log.warn("Open Router API ERROR. status = {}, body = {}", clientResponse.statusCode(), body);
                                    return new IllegalArgumentException("Open Router API ERROR: \n" + body + "\n");
                                })
                        )
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
