package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class JwtInvalidRefreshTokenException extends JwtException {

    public JwtInvalidRefreshTokenException(String refreshToken) {
        super(
            ErrorCode.AUTH_REFRESH_TOKEN_MISMATCH,
            Map.of("refreshToken", refreshToken),
            HttpStatus.UNAUTHORIZED
        );
    }
}