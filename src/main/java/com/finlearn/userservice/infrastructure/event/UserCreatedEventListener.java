package com.finlearn.userservice.infrastructure.event;

import com.finlearn.userservice.domain.user.event.UserCreatedEvent;
import com.finlearn.userservice.infrastructure.kafka.UserEventProducer;
import com.finlearn.userservice.infrastructure.kafka.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserEventProducer userEventProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("UserCreatedEvent 수신 - userId: {}, email: {}", event.userId(), event.email());
        try {
            userEventProducer.publishUserRegistered(new UserRegisteredEvent(event.userId(), event.email()));
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패 - userId: {}, error: {}", event.userId(), e.getMessage());
        }
    }
}
