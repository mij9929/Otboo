package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.recommendation.dto.internal.RecommendationContext;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.exception.ProfileNotFoundException;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.exception.WeatherNotFoundException;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecommendationContextLoader {
    private final WeatherRepository weatherRepository;
    private final ProfileRepository profileRepository;
    private final ClothesRepository clothesRepository;

    public RecommendationContext load(UUID weatherId, UUID userId) {
        Weather weather = weatherRepository.findById(weatherId)
                .orElseThrow(() -> new WeatherNotFoundException(weatherId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        List<Clothes> clothes = clothesRepository.findByOwnerId(userId);

        return new RecommendationContext (weather, profile, clothes);
    }
}
