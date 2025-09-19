package ru.practicum.recomm.aggregator.kafka.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация Kafka-топиков.
 * <p>
 * Содержит имена топиков, используемых в системе для обмена сообщениями между компонентами.
 */
@Component
@ConfigurationProperties(prefix = "collector.kafka.producer.topics")
@Validated
@Getter
@Setter
public class KafkaTopics {
    /**
     * Имя топика для оценок схожести.
     */
    @NotNull(message = "eventsSimilarity не может быть null")
    private String eventsSimilarity;
}