package com.finlearn.userservice.domain.auth.repository;

import java.time.Duration;

public interface AccessTokenBlacklistRepository {

    boolean addToBlacklist(String accessToken, Duration ttl);
}
