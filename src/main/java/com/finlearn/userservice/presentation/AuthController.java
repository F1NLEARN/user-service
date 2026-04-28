package com.finlearn.userservice.presentation;

import com.finlearn.common.response.CommonResponse;
import com.finlearn.userservice.application.AuthService;
import com.finlearn.userservice.infrastructure.jwt.JwtProvider;
import com.finlearn.userservice.infrastructure.security.CustomUserDetails;
import com.finlearn.userservice.presentation.dto.LoginRequest;
import com.finlearn.userservice.presentation.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 인증 API 컨트롤러. /api/v1/users/auth/** */
@RestController
@RequestMapping("/api/v1/users/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST "/api/v1/users/auth/login"
     *
     * @param request 이메일과 비밀번호를 담은 로그인 요청 DTO
     * @return 발급된 Access Token과 Refresh Token
     */
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(CommonResponse.success(authService.login(request)));
    }

    /**
     * POST "/api/v1/users/auth/reissue"
     *
     * @param refreshToken Authorization 헤더에 담긴 Refresh Token (Bearer 포함)
     * @return 새로 발급된 Access Token과 Refresh Token
     */
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<TokenResponse>> reissue(
            @RequestHeader(JwtProvider.AUTHORIZATION_HEADER) String refreshToken) {
        return ResponseEntity.ok(CommonResponse.success(authService.reissue(refreshToken)));
    }

    /**
     * POST "/api/v1/users/auth/logut"
     *
     * @param accessToken Authorization 헤더에 담긴 Access Token (Bearer 포함)
     * @param userDetails Spring Security가 주입하는 현재 인증된 사용자 정보
     * @return 빈 성공 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @RequestHeader(JwtProvider.AUTHORIZATION_HEADER) String accessToken,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(accessToken);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
