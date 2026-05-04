package com.finlearn.userservice.application.user.service;

import com.finlearn.userservice.application.user.dto.request.LoginRequest;
import com.finlearn.userservice.application.user.dto.request.SignUpRequest;
import com.finlearn.userservice.application.user.dto.request.TokenRefreshRequest;
import com.finlearn.userservice.application.user.dto.response.InternalUserResponse;
import com.finlearn.userservice.application.user.dto.response.LoginResponse;
import com.finlearn.userservice.application.user.dto.response.TokenRefreshResponse;
import com.finlearn.userservice.application.user.dto.response.UserMeResponse;
import com.finlearn.userservice.domain.auth.entity.RefreshToken;
import com.finlearn.userservice.domain.auth.repository.AccessTokenBlacklistRepository;
import com.finlearn.userservice.domain.auth.repository.RefreshTokenRepository;
import com.finlearn.userservice.domain.user.entity.User;
import com.finlearn.userservice.domain.user.enums.UserStatus;
import com.finlearn.userservice.domain.user.event.UserCreatedEvent;
import com.finlearn.userservice.domain.user.exception.UserErrorCode;
import com.finlearn.userservice.domain.user.exception.UserException;
import com.finlearn.userservice.domain.user.repository.UserRepository;
import com.finlearn.userservice.infrastructure.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Transactional
    public UUID signUp(SignUpRequest request) {
        validateDuplicateEmail(request.email());
        validateDuplicateNickname(request.nickname());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), encodedPassword, request.nickname());
        User savedUser = userRepository.save(user);

        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getUserId(), savedUser.getEmail()));
        return savedUser.getUserId();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        validateLoginUser(user, request.password());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole().name());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getUserId());
        LocalDateTime refreshExpiry = jwtTokenProvider.getExpiryDateTime(refreshTokenValue);

        saveOrUpdateRefreshToken(user.getUserId(), refreshTokenValue, refreshExpiry);

        return LoginResponse.of(
                user,
                accessToken,
                refreshTokenValue,
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String rawRefreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(rawRefreshToken)) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        if (!"REFRESH".equals(jwtTokenProvider.getTokenType(rawRefreshToken))) {
            throw new UserException(UserErrorCode.INVALID_TOKEN_TYPE);
        }

        UUID userId = jwtTokenProvider.getUserId(rawRefreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!savedToken.getToken().equals(rawRefreshToken)) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        if (savedToken.isExpired(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken);
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        LocalDateTime newRefreshExpiry = jwtTokenProvider.getExpiryDateTime(newRefreshToken);

        savedToken.rotate(newRefreshToken, newRefreshExpiry);

        return TokenRefreshResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    @Transactional
    public void logout(TokenRefreshRequest request) {
        String rawRefreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(rawRefreshToken)) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        UUID userId = jwtTokenProvider.getUserId(rawRefreshToken);
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void logoutByAccessToken(String authorizationHeader) {
        String accessToken = extractBearerToken(authorizationHeader);

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        if (!"ACCESS".equals(jwtTokenProvider.getTokenType(accessToken))) {
            throw new UserException(UserErrorCode.ACCESS_TOKEN_REQUIRED);
        }

        long remainingMillis = jwtTokenProvider.getRemainingExpirationMillis(accessToken);
        if (remainingMillis <= 0) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        boolean saved = accessTokenBlacklistRepository.addToBlacklist(
                accessToken,
                Duration.ofMillis(remainingMillis)
        );
        if (!saved) {
            throw new UserException(UserErrorCode.TOKEN_ALREADY_BLACKLISTED);
        }
    }

    public UserMeResponse getMyInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return UserMeResponse.from(user);
    }

    public InternalUserResponse getInternalUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return InternalUserResponse.from(user);
    }

    public InternalUserResponse getInternalUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return InternalUserResponse.from(user);
    }

    private void saveOrUpdateRefreshToken(UUID userId, String token, LocalDateTime expiresAt) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        saved -> saved.rotate(token, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.issue(userId, token, expiresAt))
                );
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private void validateLoginUser(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UserException(UserErrorCode.INACTIVE_USER);
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UserException(UserErrorCode.SUSPENDED_USER);
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix) || authorizationHeader.length() <= prefix.length()) {
            throw new UserException(UserErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        return authorizationHeader.substring(prefix.length());
    }
}
