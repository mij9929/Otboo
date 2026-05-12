package com.codeit.otboo.domain.clothes.recommendation.ai.dto;

import java.util.List;

public record OpenRouterChatRequest(
        String model,
        List<Message> messages
) {
    public static OpenRouterChatRequest of(String model, String prompt) {
        return new OpenRouterChatRequest(
                model,
                List.of(
                        new Message("system", "You are a fashion recommendation assistant. Return JSON only."),
                        new Message("user", prompt)
                )
        );
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
