package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
/*
 * LLM 실패 시, Fallback 처리
 */
public class FallbackOutFitRecommender {
    public List<Clothes> recommend(List<Clothes> clothes) {
        Map<ClothesType, List<Clothes>> clothesMap = clothes.stream()
                .collect(Collectors.groupingBy(Clothes::getType));

        List<Clothes> result = new ArrayList<>();

        pickFirst(result, clothesMap, ClothesType.TOP);
        pickFirst(result, clothesMap, ClothesType.BOTTOM);
        pickFirst(result, clothesMap, ClothesType.SHOES);

        if(result.isEmpty()) {
            return clothes.stream()
                    .limit(3)
                    .toList();
        }

        return result;
    }

    private void pickFirst(
            List<Clothes> result,
            Map<ClothesType, List<Clothes>> grouped,
            ClothesType type
    ) {
        List<Clothes> items = grouped.getOrDefault(type, List.of());
        if (!items.isEmpty()) {
            result.add(items.get(0));
        }
    }
}
