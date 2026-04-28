package com.finlearn.userservice.domain.entity;

import com.finlearn.common.domain.BaseEntity;
import com.finlearn.common.exception.AuthErrorCode;
import com.finlearn.common.exception.CustomException;
import com.finlearn.userservice.domain.enums.UserStatus;
import com.finlearn.userservice.domain.service.PasswordHasher;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Builder
    private User(String email, String password, String nickname, UserStatus status) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = status;
    }

    public static User create(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void changeStatus(UserStatus status) { this.status = status; }
    public void changePassword(String password) { this.password = password; }
    public void changeNickname(String nickname) { this.nickname = nickname; }

    /** ACTIVE 상태가 아니면 로그인을 거부한다. */
    public void validateLoginable() {
        if (this.status != UserStatus.ACTIVE) {
            throw new CustomException(
                    AuthErrorCode.LOGIN_FAILED.getMessage(),
                    AuthErrorCode.LOGIN_FAILED.getStatus()
            );
        }
    }

    /** PasswordHasher로 비밀번호를 비교하고 틀리면 예외를 던진다. */
    public void validatePassword(PasswordHasher passwordHasher, String rawPassword) {
        if (!passwordHasher.matches(rawPassword, this.password)) {
            throw new CustomException(
                    AuthErrorCode.LOGIN_FAILED.getMessage(),
                    AuthErrorCode.LOGIN_FAILED.getStatus()
            );
        }
    }
}
