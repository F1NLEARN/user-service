package com.finlearn.userservice.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

// 프로필 수정 요청 DTO
@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하여야 합니다.")
    private String nickname;

    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
    private String profileImage;

    private UUID equippedBadgeId;
}
