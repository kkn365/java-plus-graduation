package ru.practicum.recomm.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.recommendations.avro.UserActionAvro;
import ru.practicum.recomm.analyzer.service.api.UserActionService;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionsKafkaClient {
    private final UserActionService userActionService;

    @KafkaListener(
            topics = "${analyzer.kafka.consumer.topics.user-actions}",
            containerFactory = "userActionListenerContainerFactory"
    )
    public void listenActions(UserActionAvro actionAvro) {
        log.info("Получено действие пользователя: {}", actionAvro);
        userActionService.handleUserAction(actionAvro);
    }
}