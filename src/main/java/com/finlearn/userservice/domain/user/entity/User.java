package com.finlearn.userservice.domain.user.entity;

import com.finlearn.common.domain.BaseEntity;
import com.finlearn.userservice.domain.user.enums.UserRole;
import com.finlearn.userservice.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import java.util.UUID;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;          // 시즌/랭킹 도메인이 요구

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Builder
    private User(String email, String password, String nickname, UserStatus status, UserRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = status;
        this.role = role;
    }

    public static User create(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    public void changeStatus(UserStatus status) { this.status = status; }
    public void changeRole(UserRole role) { this.role = role; }
    public void changePassword(String password) { this.password = password; }
    public void changeNickname(String nickname) { this.nickname = nickname; }
}