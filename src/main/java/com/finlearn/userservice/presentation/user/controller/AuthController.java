package com.finlearn.userservice.presentation.user.controller;

import com.finlearn.common.response.CommonResponse;
import com.finlearn.userservice.application.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/logout")
    public CommonResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        userService.logoutByAccessToken(authorization);
        return CommonResponse.success("로그아웃 성공", null);
    }
}
