package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.recommendation.dto.internal.OutfitCandidate;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.RecommendationContext;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationLLMServiceImpl implements RecommendationService {
    private final RecommendationContextLoader contextLoader;

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse recommend(UUID weatherId, UUID userId) {
        RecommendationContext context = contextLoader.load(weatherId, userId);

        List<OutfitCandidate> candidates
                = context.clothes().stream()
                .map(OutfitCandidate::from)
                .toList();

        log.debug("candidates : {}", candidates );

        return RecommendationResponse.builder()
                .weatherId(weatherId)
                .userId(userId)
                .clothes(List.of())
                .build();

    }
}
