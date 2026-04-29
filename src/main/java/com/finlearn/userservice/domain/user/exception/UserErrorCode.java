package com.finlearn.userservice.domain.user.exception;

import org.springframework.http.HttpStatus;

public enum UserErrorCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_002", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER_003", "비밀번호가 올바르지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "USER_004", "비활성화된 계정입니다."),
    SUSPENDED_USER(HttpStatus.FORBIDDEN, "USER_005", "정지된 계정입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_006", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USER_007", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "USER_008", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "USER_009", "저장된 리프레시 토큰이 없습니다."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "USER_010", "리프레시 토큰이 아닙니다."),
    INVALID_AUTHORIZATION_HEADER(HttpStatus.BAD_REQUEST, "USER_011", "Authorization 헤더 형식이 올바르지 않습니다."),
    ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "USER_012", "Access Token이 필요합니다."),
    TOKEN_ALREADY_BLACKLISTED(HttpStatus.CONFLICT, "USER_013", "이미 로그아웃 처리된 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    UserErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
