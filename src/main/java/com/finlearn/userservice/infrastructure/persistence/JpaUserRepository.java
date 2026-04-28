package com.finlearn.userservice.infrastructure.persistence;

import com.finlearn.userservice.domain.entity.User;
import com.finlearn.userservice.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** UserRepository 도메인 인터페이스의 JPA 구현체. */
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
}
