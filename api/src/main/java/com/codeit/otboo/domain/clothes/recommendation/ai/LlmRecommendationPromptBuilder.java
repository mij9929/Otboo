package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import org.springframework.stereotype.Component;

@Component
public class LlmRecommendationPromptBuilder {
    public String build(LlmRecommendationRequest request) {
        return """
                당신은 날씨와 사용자의 옷장을 기반으로 의상을 추천하는 스타일 추천 AI입니다.

                규칙:
                1. 반드시 candidates에 포함된 clothesId 중에서만 선택하세요.
                2. 존재하지 않는 clothesId를 만들지 마세요.
                3. 오늘 날씨와 사용자 프로필을 고려하세요.
                4. 응답은 JSON만 반환하세요.
                5. JSON 외의 설명 문장은 절대 포함하지 마세요.

                응답 형식:
                {
                  "selectedClothesIds": ["clothesId-1", "clothesId-2"],
                  "reason": "추천 이유"
                }

                입력 데이터:
                %s
                """.formatted(request);
    }
}
