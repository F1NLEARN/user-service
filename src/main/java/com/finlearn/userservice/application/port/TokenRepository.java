package com.finlearn.userservice.application.port;

/** 토큰 저장소 포트 인터페이스. Application 계층이 Infrastructure를 직접 의존하지 않도록 추상화한다. */
public interface TokenRepository {
    void saveRefreshToken(Long userId, String token, long ttlMs);
    String getRefreshToken(Long userId);
    void deleteRefreshToken(Long userId);
    void addToBlacklist(String accessToken, long ttlMs);
    boolean isBlacklisted(String accessToken);
}
