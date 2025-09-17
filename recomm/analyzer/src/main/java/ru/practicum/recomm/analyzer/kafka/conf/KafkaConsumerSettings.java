package ru.practicum.recomm.analyzer.kafka.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Конфигурация настроек Kafka-потребителя.
 * <p>
 * Используется для хранения параметров подключения к Kafka и настройки поведения потребителя.
 */
@Component
@ConfigurationProperties(prefix = "spring.kafka.consumer")
@Getter
@Setter
public class KafkaConsumerSettings {
    /**
     * Адреса серверов Kafka (например, host:port).
     */
    private String bootstrapServers;

    /**
     * Класс десериализатора ключа сообщения.
     */
    private String keyDeserializer;

    /**
     * Класс десериализатора значения сообщения.
     */
    private String valueDeserializer;

    /**
     * Уникальный идентификатор клиента Kafka.
     */
    private String clientId;

    /**
     * Группа потребителей для координации обработки сообщений.
     */
    private String groupId;

    /**
     * Максимальное количество записей, возвращаемых за одно обращение к Kafka.
     */
    private int maxPollRecords;

    /**
     * Максимальный размер (в байтах) данных, которые могут быть получены за одно обращение.
     */
    private int fetchMaxBytes;

    /**
     * Максимальный размер (в байтах) данных, которые могут быть получены за одно обращение из одной партиции.
     */
    private int maxPartitionFetchBytes;
}