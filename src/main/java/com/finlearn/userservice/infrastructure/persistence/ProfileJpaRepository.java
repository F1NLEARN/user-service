package com.finlearn.userservice.infrastructure.persistence;

import com.finlearn.userservice.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProfileJpaRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    // 닉네임 중복 검증 (수정 요청자 본인 닉네임은 제외)
    boolean existsByNicknameAndUserIdNot(String nickname, UUID userId);
}
