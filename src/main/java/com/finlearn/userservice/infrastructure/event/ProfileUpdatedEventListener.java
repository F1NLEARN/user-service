package com.finlearn.userservice.infrastructure.event;

import com.finlearn.userservice.domain.user.event.ProfileUpdatedEvent;
import com.finlearn.userservice.infrastructure.kafka.UserEventProducer;
import com.finlearn.userservice.infrastructure.kafka.event.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileUpdatedEventListener {

    private final UserEventProducer userEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileUpdatedEvent(ProfileUpdatedEvent event) {
        log.info("ProfileUpdatedEvent 수신 - userId: {}", event.userId());
        try {
            userEventProducer.publishUserProfileUpdated(
                    new UserProfileUpdatedEvent(event.userId(), event.nickname(), event.profileImage())
            );
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패 - userId: {}, error: {}", event.userId(), e.getMessage());
        }
    }
}