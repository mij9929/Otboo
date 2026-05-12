package com.codeit.otboo.domain.clothes.recommendation.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otboo.recommendation.llm")
public record LlmRecommendationProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        int timeoutSeconds
) {
}
