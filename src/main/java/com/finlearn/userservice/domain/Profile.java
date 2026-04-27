package com.finlearn.userservice.domain;

import com.finlearn.common.domain.BaseEntity;
import com.finlearn.userservice.domain.vo.EquippedBadge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "profile_id", updatable = false, nullable = false)
    private UUID profileId;

    // users 테이블의 user_id 참조값 (JPA 연관관계 미설정)
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Embedded
    private EquippedBadge equippedBadge;


    @Builder
    private Profile(UUID userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.equippedBadge = EquippedBadge.empty();
    }

    public static Profile create(UUID userId, String nickname) {
        return Profile.builder()
                .userId(userId)
                .nickname(nickname)
                .build();
    }

    // 프로필 일괄 수정 (PATCH 시멘틱 — null 필드는 변경하지 않음
    public UpdateResult update(String newNickname, String newProfileImage, UUID newBadgeId) {
        boolean nicknameChanged = applyNicknameChange(newNickname);
        boolean imageChanged    = applyProfileImageChange(newProfileImage);
        applyEquippedBadgeChange(newBadgeId);
        return new UpdateResult(nicknameChanged, imageChanged);
    }

    // 닉네임 동일 여부 확인 (서비스에서 중복 검증 전처리에 사용)
    public boolean isSameNickname(String nickname) {
        return this.nickname.equals(nickname);
    }

    public UUID getEquippedBadgeId() {
        return (equippedBadge != null) ? equippedBadge.getBadgeId() : null;
    }


    private boolean applyNicknameChange(String newNickname) {
        if (newNickname == null || this.nickname.equals(newNickname)) return false;
        this.nickname = newNickname;
        return true;
    }

    private boolean applyProfileImageChange(String newProfileImage) {
        if (newProfileImage == null || newProfileImage.equals(this.profileImage)) return false;
        this.profileImage = newProfileImage;
        return true;
    }

    private void applyEquippedBadgeChange(UUID newBadgeId) {
        if (newBadgeId == null) return;
        this.equippedBadge = EquippedBadge.of(newBadgeId);
    }


    /**
     * update() 결과값
     * requiresSync() == true면, 타 서비스 스냅샷 동기화 필요
     */
    public record UpdateResult(boolean nicknameChanged, boolean imageChanged) {
        public boolean requiresSync() {
            return nicknameChanged || imageChanged;
        }
    }
}
