package com.finlearn.userservice.domain.user.event;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email
) {
}