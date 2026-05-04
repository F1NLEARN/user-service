package com.finlearn.userservice.application;

import com.finlearn.common.exception.ConflictException;
import com.finlearn.common.exception.NotFoundException;
import com.finlearn.userservice.domain.Profile;
import com.finlearn.userservice.domain.user.event.ProfileUpdatedEvent;
import com.finlearn.userservice.infrastructure.persistence.ProfileJpaRepository;
import com.finlearn.userservice.presentation.dto.request.UpdateProfileRequest;
import com.finlearn.userservice.presentation.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileJpaRepository profileJpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 내 프로필 조회
     * GET /api/v1/users/me/profile
     */
    public ProfileResponse getMyProfile(UUID userId) {
        return ProfileResponse.from(findProfileByUserId(userId));
    }

    /**
     * 프로필 수정
     * PATCH /api/v1/users/me/profile
     */
    @Transactional
    public ProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        Profile profile = findProfileByUserId(userId);

        if (request.getNickname() != null && !profile.isSameNickname(request.getNickname())) {
            validateNicknameDuplicate(request.getNickname(), userId);
        }

        Profile.UpdateResult result = profile.update(
                request.getNickname(),
                request.getProfileImage(),
                request.getEquippedBadgeId()
        );

        profileJpaRepository.save(profile);

        // 다른 서비스 스냅샷 동기화 필요 시 이벤트 발행
        if (result.requiresSync()) {
            publishProfileUpdatedEvent(profile);
        }

        log.info("[ProfileService] 프로필 수정: userId={}, sync={}", userId, result.requiresSync());
        return ProfileResponse.from(profile);
    }


    private Profile findProfileByUserId(UUID userId) {
        return profileJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("프로필을 찾을 수 없습니다."));
    }

    private void validateNicknameDuplicate(String nickname, UUID userId) {
        if (profileJpaRepository.existsByNicknameAndUserIdNot(nickname, userId)) {
            throw new ConflictException("이미 사용 중인 닉네임입니다.");
        }
    }

    private void publishProfileUpdatedEvent(Profile profile) {
        eventPublisher.publishEvent(new ProfileUpdatedEvent(
                profile.getUserId(),
                profile.getNickname(),
                profile.getProfileImage()
        ));
    }
}
