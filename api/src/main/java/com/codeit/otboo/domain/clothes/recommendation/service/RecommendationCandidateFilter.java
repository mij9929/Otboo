package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecommendationCandidateFilter {
    private static final int TOP_LIMIT = 10;
    private static final int BOTTOM_LIMIT = 10;
    private static final int OUTER_LIMIT = 5;
    private static final int DRESS_LIMIT = 5;
    private static final int SHOES_LIMIT = 5;
    private static final int ETC_LIMIT = 5;

    public List<Clothes> filter(List<Clothes> clothes, Weather weather) {
        Map<ClothesType, List<Clothes>> grouped = clothes.stream()
                .collect(Collectors.groupingBy(Clothes::getType));

        return grouped.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .limit(limitByType(entry.getKey())))
                .toList();
    }

    private int limitByType(ClothesType type) {
        return switch(type) {
            case TOP -> TOP_LIMIT;
            case BOTTOM -> BOTTOM_LIMIT;
            case OUTER -> OUTER_LIMIT;
            case DRESS -> DRESS_LIMIT;
            case SHOES -> SHOES_LIMIT;
            default -> ETC_LIMIT;
        };
    }
}
