package com.finlearn.userservice.application.user.dto.response;

import com.finlearn.userservice.domain.user.entity.User;
import com.finlearn.userservice.domain.user.enums.UserRole;
import com.finlearn.userservice.domain.user.enums.UserStatus;
import java.util.UUID;

public record InternalUserResponse(
        UUID userId,
        String email,
        String nickname,
        UserStatus status,
        UserRole role
) {
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus(),
                user.getRole()
        );
    }
}