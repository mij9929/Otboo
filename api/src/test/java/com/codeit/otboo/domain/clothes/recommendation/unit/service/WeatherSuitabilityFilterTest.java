package com.codeit.otboo.domain.clothes.recommendation.unit.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.recommendation.service.WeatherSuitabilityFilter;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.Weather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeatherSuitabilityFilterTest {

    private final WeatherSuitabilityFilter filter = new WeatherSuitabilityFilter();

    @Test
    @DisplayName("더운 날에는 패딩을 후보에서 제외한다")
    void hotWeather_excludesPadding() {
        Weather weather = weather(28.0, PrecipitationType.NONE);
        Profile profile = profile(3);
        Clothes shortSleeve = clothes(ClothesType.TOP, "반팔티");
        Clothes padding = clothes(ClothesType.OUTER, "패딩");

        List<Clothes> result = filter.filter(
                List.of(shortSleeve, padding),
                weather,
                profile
        );

        assertThat(result).contains(shortSleeve);
        assertThat(result).doesNotContain(padding);
    }

    @Test
    @DisplayName("추운 날에는 민소매를 후보에서 제외한다")
    void coldWeather_excludesSleevelessTop() {
        Weather weather = weather(2.0, PrecipitationType.NONE);
        Profile profile = profile(3);
        Clothes sleeveless = clothes(ClothesType.TOP, "민소매");
        Clothes turtleneck = clothes(ClothesType.TOP, "목폴라");

        List<Clothes> result = filter.filter(
                List.of(sleeveless, turtleneck),
                weather,
                profile
        );

        assertThat(result).contains(turtleneck);
        assertThat(result).doesNotContain(sleeveless);
    }

    @Test
    @DisplayName("비 오는 날에는 샌들과 슬리퍼를 후보에서 제외한다")
    void rainyWeather_excludesSandalsAndSlippers() {
        Weather weather = weather(22.0, PrecipitationType.RAIN);
        Profile profile = profile(3);
        Clothes sandals = clothes(ClothesType.SHOES, "샌들");
        Clothes slippers = clothes(ClothesType.SHOES, "슬리퍼");
        Clothes rainBoots = clothes(ClothesType.SHOES, "장화");

        List<Clothes> result = filter.filter(
                List.of(sandals, slippers, rainBoots),
                weather,
                profile
        );

        assertThat(result).contains(rainBoots);
        assertThat(result).doesNotContain(sandals, slippers);
    }

    @Test
    @DisplayName("온도 민감도가 높으면 체감 온도가 올라간 기준으로 필터링한다")
    void highTemperatureSensitivity_raisesEffectiveTemperature() {
        Weather weather = weather(17.0, PrecipitationType.NONE);
        Profile profile = profile(5);
        Clothes shortSleeve = clothes(ClothesType.TOP, "반팔티");

        List<Clothes> result = filter.filter(
                List.of(shortSleeve),
                weather,
                profile
        );

        assertThat(result).contains(shortSleeve);
    }

    @Test
    @DisplayName("이름으로 세부 타입을 판단할 수 없는 옷은 후보에 유지한다")
    void unknownClothesName_keepsCandidate() {
        Weather weather = weather(2.0, PrecipitationType.SNOW);
        Profile profile = profile(3);
        Clothes unknownTop = clothes(ClothesType.TOP, "데일리 상의");
        Clothes unknownShoes = clothes(ClothesType.SHOES, "데일리 슈즈");

        List<Clothes> result = filter.filter(
                List.of(unknownTop, unknownShoes),
                weather,
                profile
        );

        assertThat(result).containsExactly(unknownTop, unknownShoes);
    }

    private Clothes clothes(ClothesType type, String name) {
        Clothes clothes = mock(Clothes.class);
        when(clothes.getType()).thenReturn(type);
        when(clothes.getName()).thenReturn(name);
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
}
