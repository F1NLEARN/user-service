package com.finlearn.userservice.infrastructure.event;

import com.finlearn.userservice.domain.user.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserCreatedEventListener {

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("UserCreatedEvent 수신 - userId: {}, email: {}", event.userId(), event.email());

        // 1. profile-service 초기 프로필 생성 이벤트 발행
        // 2. Kafka 토픽 발행
        // 3. 감사 로그 저장
    }
}