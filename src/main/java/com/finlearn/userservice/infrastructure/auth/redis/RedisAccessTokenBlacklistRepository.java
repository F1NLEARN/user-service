package com.finlearn.userservice.infrastructure.auth.redis;

import com.finlearn.userservice.domain.auth.repository.AccessTokenBlacklistRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisAccessTokenBlacklistRepository implements AccessTokenBlacklistRepository {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean addToBlacklist(String accessToken, Duration ttl) {
        String key = BLACKLIST_PREFIX + accessToken;
        Boolean saved = redisTemplate.opsForValue().setIfAbsent(key, "LOGOUT", ttl);
        return Boolean.TRUE.equals(saved);
    }
}
