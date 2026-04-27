package com.finlearn.userservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

// 유저 프로필 변경 시 발행
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdatedEvent {

    private UUID userId;
    private String nickname;
    private String profileImage;
}
