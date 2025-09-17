package ru.practicum.recomm.analyzer.kafka.conf;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация Kafka-топиков.
 * <p>
 * Содержит имена топиков, используемых в системе для обмена сообщениями между компонентами.
 */
@Component
@ConfigurationProperties(prefix = "spring.kafka.topics")
@Validated
@Getter
public class KafkaTopics {
    /**
     * Имя топика для пользовательских действий.
     * <p>
     * Пример: "user-actions"
     */
    @NotNull(message = "userActionsTopic не может быть null")
    private String userActionsTopic;

    /**
     * Имя топика для схожести мероприятий.
     * <p>
     * Пример: "events-similarity"
     */
    @NotNull(message = "eventsSimilarityTopic не может быть null")
    private String eventsSimilarityTopic;
}