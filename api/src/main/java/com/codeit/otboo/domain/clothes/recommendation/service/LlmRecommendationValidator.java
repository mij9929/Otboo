package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LlmRecommendationValidator {
    public List<Clothes> validate(LlmRecommendationResponse llmRecommendationResponse, List<Clothes> candidates) {
        Map<UUID, Clothes> candidatesMap = candidates.stream()
                .collect(Collectors.toMap(Clothes::getId, Function.identity()));

        List<UUID> selectedClothesIds = llmRecommendationResponse.selectedClothesIds();

        if(selectedClothesIds == null || selectedClothesIds.isEmpty()) {
            log.debug("LLM 리스트 결과가 비어있음.");
            throw new IllegalArgumentException("LLM 리스트 결과가 비어있음.");
        }

        boolean hasInvalidId = selectedClothesIds.stream()
                .distinct()
                .anyMatch(id -> !candidatesMap.containsKey(id));

        if(hasInvalidId){
            log.debug("LLM 추천 결과에 유효하지 않은 의상 ID가 포함되어 있습니다.");
            throw new IllegalArgumentException("LLM 추천 결과에 유효하지 않은 의상 ID가 포함되어 있습니다.");
        }

        return selectedClothesIds.stream()
                .distinct()
                .map(candidatesMap::get)
                .toList();
    }
}
