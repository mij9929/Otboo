package com.codeit.otboo.domain.clothes.recommendation.dto.internal;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;

import java.util.List;
import java.util.UUID;

public record OutfitCandidate(
        UUID clothesId,
        ClothesType clothesType,
        String name,
        List<String> attributes
) {
    public static OutfitCandidate from(Clothes clothes) {
        return new OutfitCandidate(
                clothes.getId(),
                clothes.getType(),
                clothes.getName(),
                clothes.getValues().stream()
                        .map(ClothesAttributeValue::getSelectableValue)
                        .toList()
        );
    }
}
