package com.finlearn.userservice.presentation.dto;

import lombok.Builder;
import lombok.Getter;

/** 로그인·재발급 성공 시 반환하는 DTO. */
@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    /** 순수 JWT를 받아서 "Bearer " 접두사를 붙인다. */
    public static TokenResponse of(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken("Bearer " + accessToken)
                .refreshToken("Bearer " + refreshToken)
                .build();
    }
}
