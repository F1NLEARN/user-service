package com.finlearn.userservice.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/** 로그인 요청 DTO. */
@Getter
public class LoginRequest {

    @NotBlank(message = "이메일을 입력해야 한다.")
    @Email(message = "이메일 형식이 올바르지 않다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해야 한다.")
    private String password;
}
