package com.finlearn.userservice.presentation.dto.response;

import com.finlearn.userservice.domain.Profile;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 프로필 조회/수정 응답 DTO
 * TODO: email, status 필드는 User 엔티티 통합 후 추가 예정
 */
@Getter
@Builder
public class ProfileResponse {

    private UUID userId;
    private String nickname;
    private String profileImage;
    private UUID equippedBadgeId;
    private LocalDateTime updatedAt;

    public static ProfileResponse from(Profile profile) {
        return ProfileResponse.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .profileImage(profile.getProfileImage())
                .equippedBadgeId(profile.getEquippedBadgeId())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
