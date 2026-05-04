package com.finlearn.userservice.application.user.dto.response;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresInSec
) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken, long accessTokenExpiresInSec) {
        return new TokenRefreshResponse(accessToken, refreshToken, "Bearer", accessTokenExpiresInSec);
    }
}