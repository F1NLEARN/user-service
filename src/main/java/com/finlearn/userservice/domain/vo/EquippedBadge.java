package com.finlearn.userservice.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EquippedBadge {

    @Column(name = "equipped_badge_id")
    private UUID badgeId;

    public static EquippedBadge of(UUID badgeId) {
        return new EquippedBadge(badgeId);
    }

    public static EquippedBadge empty() {
        return new EquippedBadge(null);
    }
}
