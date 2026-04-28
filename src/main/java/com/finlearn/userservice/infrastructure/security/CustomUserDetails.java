package com.finlearn.userservice.infrastructure.security;

import com.finlearn.userservice.domain.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Spring Security 인증에서 사용하는 사용자 정보 래퍼. */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    // 로그인 시 — DB 조회 결과로 생성. 비밀번호 포함.
    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
    }

    // 필터 통과 시 — JWT 클레임으로 생성. DB 조회 없음. password null.
    public CustomUserDetails(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        this.password = null;
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
