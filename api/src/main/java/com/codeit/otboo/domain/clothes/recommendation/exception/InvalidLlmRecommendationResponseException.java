package com.codeit.otboo.domain.clothes.recommendation.exception;

public class InvalidLlmRecommendationResponseException extends RuntimeException {

    public InvalidLlmRecommendationResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLlmRecommendationResponseException(String message) {
        super(message);
    }
}