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
        long totalStartTime = System.currentTimeMillis();
        RecommendationContext context = contextLoader.load(weatherId, userId);

        List<Clothes> weatherSuitableClothes = weatherSuitabilityFilter.filter(context.clothes(), context.weather(), context.profile());

        List<Clothes> candidateClothes = recommendationCandidateLimiter
                .limit(weatherSuitableClothes);
        int candidateCount = candidateClothes.size();

        if (candidateClothes.isEmpty()) {
            RecommendationResponse response = responseAssembler.assemble(weatherId, userId, List.of());
            long totalElapsedMs = System.currentTimeMillis() - totalStartTime;
            log.info(
                    "LLM recommendation skipped. candidateCount={}, selectedCount=0, llmElapsedMs=0, totalElapsedMs={}, fallback=false, exceptionType=none, reason=no_candidates",
                    candidateCount,
                    totalElapsedMs
            );
            return response;
        }

        List<OutfitCandidate> candidates
                = candidateClothes.stream()
                .map(OutfitCandidate::from)
                .toList();

        log.debug("candidates : {}", candidates );

        List<Clothes> selectedClothes;
        long llmElapsedMs = 0;
        long llmStartTime = 0;
        boolean fallback = false;
        String exceptionType = "none";

        try{
            LlmRecommendationRequest llmRecommendationRequest = LlmRecommendationRequest.from(context, candidates);
            log.debug("llmRecommendationRequest : {}", llmRecommendationRequest );

            llmStartTime = System.currentTimeMillis();
            LlmRecommendationResponse llmRecommendationResponse = llmRecommendationClient.recommend(llmRecommendationRequest);
            llmElapsedMs = System.currentTimeMillis() - llmStartTime;
            log.debug("llmRecommendationResponse : {}", llmRecommendationResponse);

            selectedClothes =
                    llmRecommendationValidator.validate(
                            llmRecommendationResponse,
                            candidateClothes
                    );

        } catch (RuntimeException e) {
            fallback = true;
            exceptionType = e.getClass().getSimpleName();
            if (llmStartTime > 0 && llmElapsedMs == 0) {
                llmElapsedMs = System.currentTimeMillis() - llmStartTime;
            }
            log.warn(
                    "LLM 추천 실패 - Fallback 처리. exceptionType={}",
                    exceptionType
            );
            selectedClothes = fallbackOutFitRecommender.recommend(candidateClothes);
        }

        RecommendationResponse response = responseAssembler.assemble(
                weatherId,
                userId,
                selectedClothes
        );
        long totalElapsedMs = System.currentTimeMillis() - totalStartTime;
        if (fallback) {
            log.info(
                    "LLM recommendation fallback. candidateCount={}, selectedCount={}, llmElapsedMs={}, totalElapsedMs={}, fallback=true, exceptionType={}, reason=fallback",
                    candidateCount,
                    selectedClothes.size(),
                    llmElapsedMs,
                    totalElapsedMs,
                    exceptionType
            );
        } else {
            log.info(
                    "LLM recommendation completed. candidateCount={}, selectedCount={}, llmElapsedMs={}, totalElapsedMs={}, fallback=false, exceptionType=none, reason=success",
                    candidateCount,
                    selectedClothes.size(),
                    llmElapsedMs,
                    totalElapsedMs
            );
        }
        return response;

    }
}
