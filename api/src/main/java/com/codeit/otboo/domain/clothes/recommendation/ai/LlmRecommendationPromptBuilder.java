package com.codeit.otboo.domain.clothes.recommendation.ai;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import org.springframework.stereotype.Component;

@Component
public class LlmRecommendationPromptBuilder {

    public String build(LlmRecommendationRequest request) {
        return """
                당신은 날씨와 사용자의 옷장을 기반으로 의상을 추천하는 스타일 추천 AI입니다.

                규칙:
                1. 반드시 입력 데이터의 candidates에 포함된 clothesId 값만 그대로 선택하세요.
                2. clothesId는 UUID 형식의 문자열입니다.
                3. 존재하지 않는 clothesId, 임의 ID, 예시 ID, placeholder를 절대 만들지 마세요.
                4. selectedClothesIds 배열에는 candidates에 실제로 존재하는 clothesId만 넣으세요.
                5. 오늘 날씨와 사용자 프로필을 고려하세요.
                6. 응답은 JSON만 반환하세요.
                7. JSON 외의 설명 문장, markdown, ```json 코드블록은 절대 포함하지 마세요.

                응답 JSON 스키마:
                {
                  "selectedClothesIds": ["입력 candidates에 존재하는 실제 UUID clothesId"],
                  "reason": "추천 이유"
                }

                잘못된 예:
                {
                  "selectedClothesIds": ["clothesId-1", "example-id", "상의1"],
                  "reason": "..."
                }

                입력 데이터:
                %s
                """.formatted(request);
    }
}