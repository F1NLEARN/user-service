package com.finlearn.userservice.presentation;

import com.finlearn.userservice.application.ProfileService;
import com.finlearn.userservice.presentation.dto.request.UpdateProfileRequest;
import com.finlearn.userservice.presentation.dto.response.ProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * GET /api/v1/users/me/profile
     * 내 프로필 조회
     */
    @GetMapping("/me/profile")
    public ProfileResponse getMyProfile(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return profileService.getMyProfile(userId);
    }

    /**
     * PATCH /api/v1/users/me/profile
     * 프로필 수정
     */
    @PatchMapping("/me/profile")
    public ProfileResponse updateMyProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return profileService.updateMyProfile(userId, request);
    }
}
