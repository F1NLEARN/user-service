package com.finlearn.userservice.domain.user.event;

import java.util.UUID;

public record ProfileUpdatedEvent(
        UUID userId,
        String nickname,
        String profileImage
) {
}