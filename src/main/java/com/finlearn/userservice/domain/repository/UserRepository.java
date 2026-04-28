package com.finlearn.userservice.domain.repository;

import com.finlearn.userservice.domain.entity.User;

import java.util.Optional;

/** User 도메인 리포지토리 순수 인터페이스. */
public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    User save(User user);
}
