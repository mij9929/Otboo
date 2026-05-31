package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.exception.InvalidLlmRecommendationResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmRecommendationResponseParser {
    private final ObjectMapper objectMapper;

    public LlmRecommendationResponse parse(String content) {
        try {
            log.debug("LLM raw content: {}", content);
            return objectMapper.readValue(content, LlmRecommendationResponse.class);
        } catch (JsonProcessingException e) {
            throw new InvalidLlmRecommendationResponseException(
                    "LLM 추천 응답 JSON 파싱에 실패했습니다.",
                    e
            );
        }
    }
}
