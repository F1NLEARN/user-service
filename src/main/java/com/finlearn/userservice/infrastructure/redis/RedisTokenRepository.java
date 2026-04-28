package com.finlearn.userservice.infrastructure.redis;

import com.finlearn.userservice.application.port.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/** TokenRepository의 Redis 구현체. */
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";

    @Override
    public void saveRefreshToken(Long userId, String token, long ttlMs) {
        redisTemplate.opsForValue().set(RT_PREFIX + userId, token, ttlMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getRefreshToken(Long userId) {
        return (String) redisTemplate.opsForValue().get(RT_PREFIX + userId);
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(RT_PREFIX + userId);
    }

    @Override
    public void addToBlacklist(String accessToken, long ttlMs) {
        redisTemplate.opsForValue().set(BL_PREFIX + accessToken, "logout", ttlMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BL_PREFIX + accessToken));
    }
}
