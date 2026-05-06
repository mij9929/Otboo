package com.codeit.otboo.domain.clothes.recommendation.dto.internal;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.weather.entity.Weather;

import java.util.List;

public record RecommendationContext(
        Weather weather,
        Profile profile,
        List<Clothes> clothes
) {
}
