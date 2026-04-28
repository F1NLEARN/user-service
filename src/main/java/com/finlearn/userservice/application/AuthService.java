package com.finlearn.userservice.application;

import com.finlearn.common.exception.AuthErrorCode;
import com.finlearn.common.exception.CustomException;
import com.finlearn.userservice.application.query.UserQueryService;
import com.finlearn.userservice.domain.entity.User;
import com.finlearn.userservice.domain.service.PasswordHasher;
import com.finlearn.userservice.infrastructure.jwt.JwtProvider;
import com.finlearn.userservice.application.port.TokenRepository;
import com.finlearn.userservice.presentation.dto.LoginRequest;
import com.finlearn.userservice.presentation.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserQueryService userQueryService;
    private final PasswordHasher passwordHasher;
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    // 로그인
    public TokenResponse login(LoginRequest request) {
        User user = userQueryService.getByEmail(request.getEmail()); // 유저 조회
        user.validateLoginable();                                              // 계정 상태 검증
        user.validatePassword(passwordHasher, request.getPassword());          // 비밀번호 검증

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail()); // AT 발급
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail()); // RT 발급

        tokenRepository.saveRefreshToken(user.getId(), refreshToken, jwtProvider.getExpiration(refreshToken)); // RT Redis 저장

        return TokenResponse.of(accessToken, refreshToken);
    }

    // 재발급
    public TokenResponse reissue(String bearerToken) {
        String refreshToken = jwtProvider.resolveToken(bearerToken); // Bearer 제거

        Claims claims;
        try {
            claims = jwtProvider.getClaims(refreshToken); // RT 검증 및 파싱
        } catch (Exception e) {
            throw new CustomException(
                    AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage(),
                    AuthErrorCode.INVALID_REFRESH_TOKEN.getStatus()
            );
        }

        Long userId = jwtProvider.getUserId(claims);
        String storedToken = tokenRepository.getRefreshToken(userId); // Redis에서 저장된 RT 조회

        if (!refreshToken.equals(storedToken)) { // 불일치 시 탈취된 토큰으로 판단
            throw new CustomException(
                    AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage(),
                    AuthErrorCode.INVALID_REFRESH_TOKEN.getStatus()
            );
        }

        String newAccessToken = jwtProvider.createAccessToken(userId, jwtProvider.getEmail(claims)); // 새 AT 발급
        String newRefreshToken = jwtProvider.createRefreshToken(userId, jwtProvider.getEmail(claims)); // 새 RT 발급

        tokenRepository.saveRefreshToken(userId, newRefreshToken, jwtProvider.getExpiration(newRefreshToken)); // RT Rotation

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    // 로그아웃
    public void logout(String bearerToken) {
        String accessToken = jwtProvider.resolveToken(bearerToken); // Bearer 제거
        Claims claims = jwtProvider.getClaims(accessToken); // AT 파싱

        tokenRepository.addToBlacklist(accessToken, jwtProvider.getExpiration(accessToken)); // AT 블랙리스트 등록
        tokenRepository.deleteRefreshToken(jwtProvider.getUserId(claims)); // RT 삭제
    }
}
