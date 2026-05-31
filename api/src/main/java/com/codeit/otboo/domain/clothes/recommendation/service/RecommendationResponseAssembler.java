package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendationResponseAssembler {
    private final ClothesMapper clothesMapper;
    private final BinaryContentUrlResolver binaryContentUrlResolver;

    public RecommendationResponse assemble(
            UUID weatherId,
            UUID userId,
            List<Clothes> selectedClothes
    ) {
        Map<UUID, List<String>> groupingMap = buildAttributeMap(selectedClothes);

        return RecommendationResponse.builder()
                .weatherId(weatherId)
                .userId(userId)
                .clothes(
                        selectedClothes.stream()
                                .map(clothes -> clothesMapper.toDto(
                                        clothes,
                                        resolveImageUrl(clothes.getBinaryContent()),
                                        groupingMap
                                ))
                                .toList()
                )
                .build();
    }

    private Map<UUID, List<String>> buildAttributeMap(List<Clothes> clothes) {
        return clothes.stream()
                .flatMap(cloth -> cloth.getValues().stream())
                .collect(Collectors.groupingBy(
                        value -> value.getAttributeDef().getId(),
                        Collectors.mapping(
                                ClothesAttributeValue::getSelectableValue,
                                Collectors.toList()
                        )
                ));
    }

    private String resolveImageUrl(BinaryContent binaryContent) {
        if (binaryContent == null) {
            return null;
        }
        return binaryContentUrlResolver.resolve(binaryContent.getId());
    }
}
