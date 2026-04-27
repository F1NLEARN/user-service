package com.finlearn.userservice.domain.user.exception;

import com.finlearn.common.exception.CustomException;

public class UserException extends CustomException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public UserErrorCode getErrorCode() {
        return errorCode;
    }
}