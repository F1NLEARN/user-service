package com.finlearn.userservice.application;

import com.finlearn.common.exception.ConflictException;
import com.finlearn.common.exception.NotFoundException;
import com.finlearn.userservice.domain.Profile;
import com.finlearn.userservice.infrastructure.kafka.UserEventProducer;
import com.finlearn.userservice.infrastructure.kafka.event.UserProfileUpdatedEvent;
import com.finlearn.userservice.infrastructure.persistence.ProfileJpaRepository;
import com.finlearn.userservice.presentation.dto.request.UpdateProfileRequest;
import com.finlearn.userservice.presentation.dto.response.ProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private ProfileJpaRepository profileJpaRepository;

    @Mock
    private UserEventProducer userEventProducer;

    private UUID userId;
    private Profile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profile = Profile.create(userId, "테스트 유저");
        ReflectionTestUtils.setField(profile, "profileImage",
                "https://cdn.finlearn.io/images/profile.png");
    }

    // ──────────────────────────────────────────────────────────────
    // 프로필 조회
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("프로필 조회")
    class GetMyProfile {

        @Test
        @DisplayName("닉네임, 프로필 이미지, 착용 뱃지 정보가 정상 반환된다")
        void success() {
            UUID badgeId = UUID.randomUUID();
            profile.update(null, null, badgeId);
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            ProfileResponse response = profileService.getMyProfile(userId);

            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getNickname()).isEqualTo("테스트 유저");
            assertThat(response.getProfileImage()).isEqualTo("https://cdn.finlearn.io/images/profile.png");
            assertThat(response.getEquippedBadgeId()).isEqualTo(badgeId);
        }

        @Test
        @DisplayName("프로필이 없으면 NotFoundException 발생")
        void notFound() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getMyProfile(userId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 닉네임 중복 검증
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("닉네임 중복 검증")
    class NicknameDuplicate {

        @Test
        @DisplayName("이미 존재하는 닉네임으로 변경 시도 시 ConflictException 발생")
        void duplicate_throwsConflict() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(profileJpaRepository.existsByNicknameAndUserIdNot("중복닉네임", userId))
                    .willReturn(true);

            assertThatThrownBy(() ->
                    profileService.updateMyProfile(userId, request("중복닉네임", null, null)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("이미 사용 중인 닉네임");

            verify(userEventProducer, never()).publishUserProfileUpdated(any());
        }

        @Test
        @DisplayName("현재와 동일한 닉네임으로 수정 시 DB 중복 조회를 하지 않습니다.")
        void sameNickname_skipsDbCheck() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(profileJpaRepository.save(any())).willReturn(profile);

            // 현재 닉네임과 동일
            profileService.updateMyProfile(userId, request("테스트 유저", null, null));

            // existsByNicknameAndUserIdNot 호출 없음
            verify(profileJpaRepository, never()).existsByNicknameAndUserIdNot(any(), any());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 이벤트 발행 조건
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UserProfileUpdated 이벤트 발행 조건")
    class EventPublish {

        @Test
        @DisplayName("닉네임 변경 시 이벤트가 발행됩니다.")
        void nicknameChanged_publishesEvent() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(profileJpaRepository.existsByNicknameAndUserIdNot("새닉네임", userId))
                    .willReturn(false);
            given(profileJpaRepository.save(any())).willReturn(profile);

            profileService.updateMyProfile(userId, request("새닉네임", null, null));

            ArgumentCaptor<UserProfileUpdatedEvent> captor =
                    ArgumentCaptor.forClass(UserProfileUpdatedEvent.class);
            verify(userEventProducer).publishUserProfileUpdated(captor.capture());
            assertThat(captor.getValue().getNickname()).isEqualTo("새닉네임");
        }

        @Test
        @DisplayName("프로필 이미지 변경 시 이벤트가 발행됩니다.")
        void imageChanged_publishesEvent() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(profileJpaRepository.save(any())).willReturn(profile);

            profileService.updateMyProfile(userId,
                    request(null, "https://cdn.finlearn.io/images/new.png", null));

            verify(userEventProducer).publishUserProfileUpdated(any());
        }

        @Test
        @DisplayName("착용 뱃지만 변경 시 이벤트가 발행되지 않습니다.")
        void onlyBadgeChanged_noEvent() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(profileJpaRepository.save(any())).willReturn(profile);

            profileService.updateMyProfile(userId, request(null, null, UUID.randomUUID()));

            verify(userEventProducer, never()).publishUserProfileUpdated(any());
        }

        @Test
        @DisplayName("닉네임과 착용 뱃지 동시 변경 시 이벤트가 발행됩니다.")
        void nicknameAndBadgeChanged_publishesEvent() {
            given(profileJpaRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(profileJpaRepository.existsByNicknameAndUserIdNot("새닉네임2", userId))
                    .willReturn(false);
            given(profileJpaRepository.save(any())).willReturn(profile);

            profileService.updateMyProfile(userId, request("새닉네임2", null, UUID.randomUUID()));

            verify(userEventProducer).publishUserProfileUpdated(any());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 헬퍼
    // ──────────────────────────────────────────────────────────────

    private UpdateProfileRequest request(String nickname, String profileImage, UUID equippedBadgeId) {
        UpdateProfileRequest req = new UpdateProfileRequest();
        ReflectionTestUtils.setField(req, "nickname", nickname);
        ReflectionTestUtils.setField(req, "profileImage", profileImage);
        ReflectionTestUtils.setField(req, "equippedBadgeId", equippedBadgeId);
        return req;
    }
}
