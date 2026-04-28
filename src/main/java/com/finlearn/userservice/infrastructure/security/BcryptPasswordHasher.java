package com.finlearn.userservice.infrastructure.security;

import com.finlearn.userservice.domain.service.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** PasswordHasher의 BCrypt 구현체. */
@Component
@RequiredArgsConstructor
public class BcryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
