package com.finlearn.userservice.infrastructure.jwt;

import com.finlearn.common.exception.AuthErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/** JWT 토큰 생성·검증·파싱 컴포넌트. */
@Component
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String secretKeyPlain;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key secretKey;

    @PostConstruct
    private void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKeyPlain.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // AccessToken 생성
    public String createAccessToken(Long userId, String email) {
        return buildToken(userId, email, accessTokenExpiration);
    }

    // RefreshToken 생성
    public String createRefreshToken(Long userId, String email) {
        return buildToken(userId, email, refreshTokenExpiration);
    }

    private String buildToken(Long userId, String email, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 검증 실패 시 AuthErrorCode 이름을 메시지로 담은 JwtException을 던진다.
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException(AuthErrorCode.EXPIRED_ACCESS_TOKEN.name());
        } catch (JwtException e) {
            throw new JwtException(AuthErrorCode.INVALID_ACCESS_TOKEN.name());
        }
    }

    public Long getUserId(Claims claims) { return Long.valueOf(claims.getSubject()); }
    public String getEmail(Claims claims) { return claims.get("email", String.class); }

    public String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // 남은 만료 시간(ms). Redis TTL 계산에 사용한다.
    public long getExpiration(String token) {
        return getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }
}
