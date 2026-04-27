package com.finlearn.userservice.presentation.user.controller;

import com.finlearn.common.response.CommonResponse;
import com.finlearn.userservice.application.user.dto.response.InternalUserResponse;
import com.finlearn.userservice.application.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public CommonResponse<InternalUserResponse> getByUserId(@PathVariable UUID userId) {
        return CommonResponse.success("내부 사용자 조회 성공", userService.getInternalUserById(userId));
    }

    @GetMapping("/by-email")
    public CommonResponse<InternalUserResponse> getByEmail(@RequestParam String email) {
        return CommonResponse.success("내부 사용자 조회 성공", userService.getInternalUserByEmail(email));
    }
}
