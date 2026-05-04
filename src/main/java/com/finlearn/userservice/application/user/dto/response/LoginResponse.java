package com.finlearn.userservice.application.user.dto.response;

import com.finlearn.userservice.domain.user.entity.User;
import com.finlearn.userservice.domain.user.enums.UserRole;
import com.finlearn.userservice.domain.user.enums.UserStatus;
import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String email,
        String nickname,
        UserStatus status,
        UserRole role,
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresInSec
) {
    public static LoginResponse of(
            User user,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresInSec
    ) {
        return new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus(),
                user.getRole(),
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiresInSec
        );
    }
}
