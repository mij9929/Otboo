package com.codeit.otboo.domain.clothes.recommendation.unit.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.recommendation.ai.LlmRecommendationClient;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationRequest;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.LlmRecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.RecommendationContext;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.service.FallbackOutFitRecommender;
import com.codeit.otboo.domain.clothes.recommendation.service.LlmRecommendationValidator;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationCandidateFilter;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationContextLoader;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationLLMServiceImpl;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationResponseAssembler;
import com.codeit.otboo.domain.clothes.recommendation.service.WeatherSuitabilityFilter;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.Weather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RecommendationLLMServiceImplTest {

    @Mock
    private RecommendationContextLoader contextLoader;

    @Mock
    private LlmRecommendationClient llmRecommendationClient;

    @Mock
    private LlmRecommendationValidator llmRecommendationValidator;

    @Mock
    private RecommendationResponseAssembler responseAssembler;

    @Mock
    private FallbackOutFitRecommender fallbackOutFitRecommender;

    private final RecommendationCandidateFilter recommendationCandidateFilter =
            new RecommendationCandidateFilter();

    private final WeatherSuitabilityFilter weatherSuitabilityFilter =
            new WeatherSuitabilityFilter();

    @Test
    @DisplayName("LLM 요청 후보에는 날씨 필터를 통과한 옷만 포함된다")
    void recommend_passesWeatherSuitableCandidatesToLlm() {
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Clothes shortSleeve = clothes(ClothesType.TOP, "반팔티");
        Clothes padding = clothes(ClothesType.OUTER, "패딩");
        Weather weather = weather(28.0, PrecipitationType.NONE);
        Profile profile = profile(3);

        RecommendationContext context = new RecommendationContext(
                weather,
                profile,
                List.of(shortSleeve, padding)
        );
        LlmRecommendationResponse llmResponse = new LlmRecommendationResponse(
                List.of(idOf(shortSleeve)),
                "더운 날씨에 맞는 추천입니다."
        );
        RecommendationResponse response = RecommendationResponse.builder()
                .weatherId(weatherId)
                .userId(userId)
                .clothes(List.of())
                .build();

        RecommendationLLMServiceImpl service = new RecommendationLLMServiceImpl(
                contextLoader,
                llmRecommendationClient,
                llmRecommendationValidator,
                responseAssembler,
                fallbackOutFitRecommender,
                recommendationCandidateFilter,
                weatherSuitabilityFilter
        );

        when(contextLoader.load(weatherId, userId)).thenReturn(context);
        when(llmRecommendationClient.recommend(any())).thenReturn(llmResponse);
        when(llmRecommendationValidator.validate(eq(llmResponse), any()))
                .thenReturn(List.of(shortSleeve));
        when(responseAssembler.assemble(weatherId, userId, List.of(shortSleeve)))
                .thenReturn(response);

        service.recommend(weatherId, userId);

        ArgumentCaptor<LlmRecommendationRequest> requestCaptor =
                ArgumentCaptor.forClass(LlmRecommendationRequest.class);
        verify(llmRecommendationClient).recommend(requestCaptor.capture());

        List<UUID> candidateIds = requestCaptor.getValue().candidates().stream()
                .map(candidate -> candidate.clothesId())
                .toList();

        assertThat(candidateIds).contains(idOf(shortSleeve));
        assertThat(candidateIds).doesNotContain(idOf(padding));
    }

    private Clothes clothes(ClothesType type, String name) {
        Clothes clothes = mock(Clothes.class);
        UUID id = UUID.randomUUID();
        when(clothes.getId()).thenReturn(id);
        when(clothes.getType()).thenReturn(type);
        when(clothes.getName()).thenReturn(name);
        lenient().when(clothes.getValues()).thenReturn(List.of());
        return clothes;
    }

    private Weather weather(double temperature, PrecipitationType precipitationType) {
        Weather weather = mock(Weather.class);
        when(weather.getTemperatureCurrent()).thenReturn(temperature);
        when(weather.getPrecipitationType()).thenReturn(precipitationType);
        return weather;
    }

    private Profile profile(int temperatureSensitivity) {
        Profile profile = mock(Profile.class);
        when(profile.getTemperatureSensitivity()).thenReturn(temperatureSensitivity);
        return profile;
    }

    private UUID idOf(Clothes clothes) {
        return clothes.getId();
    }
}
