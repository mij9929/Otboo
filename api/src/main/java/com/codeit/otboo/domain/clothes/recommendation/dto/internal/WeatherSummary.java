package com.codeit.otboo.domain.clothes.recommendation.dto.internal;

import com.codeit.otboo.domain.weather.entity.Weather;

public record WeatherSummary(
        Double temperature,
        String precipitationType,
        Double humidity,
        Double windSpeed
) {
    public static WeatherSummary from(Weather weather) {
        return new WeatherSummary(
                weather.getTemperatureCurrent(),
                weather.getPrecipitationType().name(),
                weather.getHumidityCurrent(),
                weather.getWindSpeed()
        );
    }
}
