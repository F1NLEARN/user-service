package com.finlearn.userservice.infrastructure.security;

import com.finlearn.common.exception.AuthErrorCode;
import com.finlearn.common.exception.CustomException;
import com.finlearn.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Spring Security 로그인 시 사용자 정보를 조회하는 서비스. JWT 필터에서는 사용하지 않는다. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new CustomException(
                        AuthErrorCode.LOGIN_FAILED.getMessage(),
                        AuthErrorCode.LOGIN_FAILED.getStatus()
                ));
    }
}
