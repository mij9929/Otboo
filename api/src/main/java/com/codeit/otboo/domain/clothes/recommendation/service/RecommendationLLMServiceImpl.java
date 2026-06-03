package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.recommendation.ai.LlmRecommendationClient;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
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
    private final LlmRecommendationClient llmRecommendationClient;
    private final LlmRecommendationValidator llmRecommendationValidator;
    private final RecommendationResponseAssembler responseAssembler;
    private final FallbackOutFitRecommender fallbackOutFitRecommender;
    private final RecommendationCandidateLimiter recommendationCandidateLimiter;
    private final WeatherSuitabilityFilter weatherSuitabilityFilter;

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse recommend(UUID weatherId, UUID userId) {
        RecommendationContext context = contextLoader.load(weatherId, userId);

        List<Clothes> weatherSuitableClothes = weatherSuitabilityFilter.filter(context.clothes(), context.weather(), context.profile());

        List<Clothes> candidateClothes = recommendationCandidateLimiter
                .limit(weatherSuitableClothes);

        if (candidateClothes.isEmpty()) {
            return responseAssembler.assemble(weatherId, userId, List.of());
        }

        List<OutfitCandidate> candidates
                = candidateClothes.stream()
                .map(OutfitCandidate::from)
                .toList();

        log.debug("candidates : {}", candidates );

        List<Clothes> selectedClothes;

        try{
            LlmRecommendationRequest llmRecommendationRequest = LlmRecommendationRequest.from(context, candidates);
            log.debug("llmRecommendationRequest : {}", llmRecommendationRequest );

            LlmRecommendationResponse llmRecommendationResponse = llmRecommendationClient.recommend(llmRecommendationRequest);
            log.debug("llmRecommendationResponse : {}", llmRecommendationResponse);

            selectedClothes =
                    llmRecommendationValidator.validate(
                            llmRecommendationResponse,
                            candidateClothes
                    );

        } catch (RuntimeException e) {
            log.warn("LLM 추천 실패 - Fallback 처리, userId = {}, weatherId = {}", userId, weatherId, e);
            selectedClothes = fallbackOutFitRecommender.recommend(candidateClothes);
        }

        return responseAssembler.assemble(
                weatherId,
                userId,
                selectedClothes
        );

    }
}
