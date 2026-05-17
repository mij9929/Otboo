package com.codeit.otboo.domain.clothes.recommendation.ai.dto;

import java.util.List;
import java.util.Map;

public record OpenRouterChatRequest(
        String model,
        List<Message> messages,
        Map<String, String> response_format
) {

    public static OpenRouterChatRequest of(String model, String prompt) {
        return new OpenRouterChatRequest(
                model,
                List.of(
                        new Message(
                                "system",
                                "You are a fashion recommendation assistant. Return JSON only."
                        ),
                        new Message("user", prompt)
                ),
                Map.of("type", "json_object")
        );
    }

    public record Message(
            String role,
            String content
    ) {
    }
}