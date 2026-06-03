package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.recommendation.service.type.DressType;
import com.codeit.otboo.domain.clothes.recommendation.service.type.OuterType;
import com.codeit.otboo.domain.clothes.recommendation.service.type.ShoesType;
import com.codeit.otboo.domain.clothes.recommendation.service.type.TopType;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class WeatherSuitabilityFilter {

    private static final int HOT = 25;
    private static final int WARM = 18;
    private static final int COOL = 10;
    private static final int COLD = 4;

    public List<Clothes> filter(
            List<Clothes> clothes,
            Weather weather,
            Profile profile
    ) {
        double effectiveTemp = getEffectiveTemp(weather, profile);

        return clothes.stream()
                .filter(clothesItem -> isWearable(clothesItem, weather, effectiveTemp))
                .toList();
    }

    private boolean isWearable(
            Clothes clothes,
            Weather weather,
            double effectiveTemp
    ) {
        return switch (clothes.getType()) {
            case TOP -> isValidTop(clothes.getName(), effectiveTemp);
            case DRESS -> isValidDress(clothes.getName(), effectiveTemp);
            case OUTER -> isValidOuter(clothes.getName(), effectiveTemp);
            case SHOES -> isValidShoes(clothes.getName(), weather, effectiveTemp);
            default -> true;
        };
    }

    private double getEffectiveTemp(Weather weather, Profile profile) {
        return weather.getTemperatureCurrent() + (profile.getTemperatureSensitivity() - 3);
    }

    private boolean isValidTop(String name, double temp) {
        TopType type = TopType.from(normalize(name));
        if (type == null) {
            return true;
        }

        return switch (type) {
            case SHORT_SLEEVE -> temp >= WARM;
            case SLEEVELESS -> temp >= HOT;
            case LONG_SLEEVE -> temp < HOT;
            case KNIT -> temp < WARM;
            case SWEATSHIRT -> temp < HOT;
            case HOODIE -> temp < HOT;
            case TURTLENECK -> temp < COLD;
        };
    }

    private boolean isValidDress(String name, double temp) {
        DressType type = DressType.from(normalize(name));
        if (type == null) {
            return true;
        }

        return switch (type) {
            case SHORT_SLEEVE -> temp >= WARM;
            case SLEEVELESS -> temp >= HOT;
            case LONG_SLEEVE -> temp < HOT;
            case KNIT -> temp < WARM;
        };
    }

    private boolean isValidOuter(String name, double temp) {
        OuterType type = OuterType.from(normalize(name));
        if (type == null) {
            return true;
        }

        return switch (type) {
            case CARDIGAN -> temp < HOT && temp >= WARM;
            case JACKET -> temp < WARM && temp >= COLD;
            case COAT -> temp < WARM && temp >= COLD;
            case PADDING -> temp < COOL;
        };
    }

    private boolean isValidShoes(String name, Weather weather, double temp) {
        ShoesType type = ShoesType.from(normalize(name));
        if (type == null || type == ShoesType.ANY) {
            return true;
        }

        Set<ShoesType> allowed = getShoesByTemp(temp);

        switch (weather.getPrecipitationType()) {
            case RAIN, SHOWER, RAIN_SNOW -> {
                allowed.add(ShoesType.RAIN_BOOTS);
                allowed.add(ShoesType.ANY);
                allowed.remove(ShoesType.SANDALS);
                allowed.remove(ShoesType.SLIPPERS);
                allowed.remove(ShoesType.FORMAL);
            }
            case SNOW -> {
                allowed.add(ShoesType.WINTER_BOOTS);
                allowed.add(ShoesType.ANY);
                allowed.remove(ShoesType.SANDALS);
                allowed.remove(ShoesType.SLIPPERS);
                allowed.remove(ShoesType.FORMAL);
            }
            case NONE -> {
                allowed.add(ShoesType.SANDALS);
                allowed.add(ShoesType.FORMAL);
                allowed.add(ShoesType.BOOTS);
                allowed.add(ShoesType.SNEAKERS);
                allowed.add(ShoesType.SLIPPERS);
                allowed.add(ShoesType.ANY);
            }
        }

        return allowed.contains(type);
    }

    private Set<ShoesType> getShoesByTemp(double temp) {
        Set<ShoesType> result = new HashSet<>();
        result.add(ShoesType.SNEAKERS);

        if (temp >= HOT) {
            result.add(ShoesType.SANDALS);
            result.add(ShoesType.SLIPPERS);
            result.add(ShoesType.ANY);
        } else if (temp >= WARM) {
            result.add(ShoesType.FORMAL);
            result.add(ShoesType.BOOTS);
            result.add(ShoesType.SLIPPERS);
            result.add(ShoesType.ANY);
        } else if (temp >= COOL) {
            result.add(ShoesType.BOOTS);
            result.add(ShoesType.FORMAL);
            result.add(ShoesType.ANY);
        } else {
            result.add(ShoesType.BOOTS);
            result.add(ShoesType.WINTER_BOOTS);
        }

        return result;
    }

    private String normalize(String name) {
        return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }
}
