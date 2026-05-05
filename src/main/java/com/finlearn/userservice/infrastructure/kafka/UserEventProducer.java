package com.finlearn.userservice.infrastructure.kafka;

import com.finlearn.userservice.infrastructure.kafka.event.UserProfileUpdatedEvent;
import com.finlearn.userservice.infrastructure.kafka.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// 유저 도메인 이벤트 Kafka 발행 클래스
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private static final String TOPIC_USER_PROFILE_UPDATED = "user.profile-updated";

    @Value("${kafka.topics.user.registered:finlearn-user-registered}")
    private String topicUserRegistered;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 회원가입 시 발행
    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("[Kafka] UserRegistered 이벤트 발행: userId={}, email={}", event.getUserId(), event.getEmail());
        kafkaTemplate.send(topicUserRegistered, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] UserRegistered 이벤트 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("[Kafka] UserRegistered 이벤트 발행 완료: topic={}, offset={}",
                                topicUserRegistered, result.getRecordMetadata().offset());
                    }
                });
    }

    // 닉네임 또는 프로필 이미지 변경 시 발행
    public void publishUserProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("[Kafka] UserProfileUpdated 이벤트 발행: userId={}, nickname={}",
                event.getUserId(), event.getNickname());
        kafkaTemplate.send(TOPIC_USER_PROFILE_UPDATED, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] UserProfileUpdated 이벤트 발행 실패: {}", ex.getMessage());
                    } else {
                        log.debug("[Kafka] UserProfileUpdated 이벤트 발행 완료: offset={}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
