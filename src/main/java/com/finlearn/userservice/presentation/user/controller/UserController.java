package com.finlearn.userservice.presentation.user.controller;

import com.finlearn.common.response.CommonResponse;
import com.finlearn.userservice.application.user.dto.request.LoginRequest;
import com.finlearn.userservice.application.user.dto.request.SignUpRequest;
import com.finlearn.userservice.application.user.dto.request.TokenRefreshRequest;
import com.finlearn.userservice.application.user.dto.response.LoginResponse;
import com.finlearn.userservice.application.user.dto.response.TokenRefreshResponse;
import com.finlearn.userservice.application.user.dto.response.UserMeResponse;
import com.finlearn.userservice.application.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public CommonResponse<UUID> signUp(@Valid @RequestBody SignUpRequest request) {
        UUID userId = userService.signUp(request);
        return CommonResponse.success("회원가입 성공", userId);
    }

    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return CommonResponse.success("로그인 성공", userService.login(request));
    }

    @PostMapping("/refresh")
    public CommonResponse<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return CommonResponse.success("토큰 재발급 성공", userService.refresh(request));
    }

    @PostMapping("/logout")
    public CommonResponse<Void> logout(@Valid @RequestBody TokenRefreshRequest request) {
        userService.logout(request);
        return CommonResponse.success("로그아웃 성공", null);
    }

    @GetMapping("/me")
    public CommonResponse<UserMeResponse> getMyInfo(@RequestHeader("X-User-Id") UUID userId) {
        return CommonResponse.success("내 정보 조회 성공", userService.getMyInfo(userId));
    }
}
