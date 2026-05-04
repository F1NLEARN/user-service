package com.finlearn.userservice.application.user.service;

import com.finlearn.userservice.domain.auth.repository.AccessTokenBlacklistRepository;
import com.finlearn.userservice.domain.auth.repository.RefreshTokenRepository;
import com.finlearn.userservice.domain.user.exception.UserErrorCode;
import com.finlearn.userservice.domain.user.exception.UserException;
import com.finlearn.userservice.domain.user.repository.UserRepository;
import com.finlearn.userservice.infrastructure.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void logoutByAccessToken_success() {
        String authorization = "Bearer test-access-token";
        String accessToken = "test-access-token";

        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(accessToken)).thenReturn("ACCESS");
        when(jwtTokenProvider.getRemainingExpirationMillis(accessToken)).thenReturn(60_000L);
        when(accessTokenBlacklistRepository.addToBlacklist(eq(accessToken), any())).thenReturn(true);

        assertDoesNotThrow(() -> userService.logoutByAccessToken(authorization));
        verify(accessTokenBlacklistRepository).addToBlacklist(eq(accessToken), any());
    }

    @Test
    void logoutByAccessToken_fail_whenAuthorizationHeaderInvalid() {
        UserException exception = assertThrows(
                UserException.class,
                () -> userService.logoutByAccessToken("invalid-header")
        );
        assertEquals(UserErrorCode.INVALID_AUTHORIZATION_HEADER, exception.getErrorCode());
    }

    @Test
    void logoutByAccessToken_fail_whenTokenInvalid() {
        String accessToken = "invalid-token";
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

        UserException exception = assertThrows(
                UserException.class,
                () -> userService.logoutByAccessToken("Bearer " + accessToken)
        );
        assertEquals(UserErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    void logoutByAccessToken_fail_whenTokenTypeIsNotAccess() {
        String accessToken = "refresh-token";
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(accessToken)).thenReturn("REFRESH");

        UserException exception = assertThrows(
                UserException.class,
                () -> userService.logoutByAccessToken("Bearer " + accessToken)
        );
        assertEquals(UserErrorCode.ACCESS_TOKEN_REQUIRED, exception.getErrorCode());
    }

    @Test
    void logoutByAccessToken_fail_whenAlreadyBlacklisted() {
        String accessToken = "already-blacklisted-token";
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(accessToken)).thenReturn("ACCESS");
        when(jwtTokenProvider.getRemainingExpirationMillis(accessToken)).thenReturn(60_000L);
        when(accessTokenBlacklistRepository.addToBlacklist(eq(accessToken), any())).thenReturn(false);

        UserException exception = assertThrows(
                UserException.class,
                () -> userService.logoutByAccessToken("Bearer " + accessToken)
        );
        assertEquals(UserErrorCode.TOKEN_ALREADY_BLACKLISTED, exception.getErrorCode());
    }

    @Test
    void logoutByAccessToken_fail_whenTokenAlreadyExpired() {
        String accessToken = "expired-token";
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getTokenType(accessToken)).thenReturn("ACCESS");
        when(jwtTokenProvider.getRemainingExpirationMillis(accessToken)).thenReturn(0L);

        UserException exception = assertThrows(
                UserException.class,
                () -> userService.logoutByAccessToken("Bearer " + accessToken)
        );
        assertEquals(UserErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }
}
