package com.finlearn.userservice.application.query;

import com.finlearn.common.exception.AuthErrorCode;
import com.finlearn.common.exception.CustomException;
import com.finlearn.userservice.domain.entity.User;
import com.finlearn.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** User 조회 전담 서비스. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    // email로 유저 단건 조회
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        AuthErrorCode.LOGIN_FAILED.getMessage(),
                        AuthErrorCode.LOGIN_FAILED.getStatus()
                ));
    }
}
