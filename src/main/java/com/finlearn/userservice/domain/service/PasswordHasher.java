package com.finlearn.userservice.domain.service;

/** 비밀번호 해시 포트 인터페이스. */
public interface PasswordHasher {
    boolean matches(String rawPassword, String encodedPassword);
    String encode(String rawPassword);
}
