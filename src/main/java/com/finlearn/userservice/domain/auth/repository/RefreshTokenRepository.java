package com.finlearn.userservice.domain.auth.repository;

import com.finlearn.userservice.domain.auth.entity.RefreshToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
