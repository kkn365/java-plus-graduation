package ru.practicum.recomm.analyzer.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.recomm.analyzer.kafka.conf.KafkaConsumerSettings;

import java.util.Properties;

/**
 * Конфигурация Kafka-потребителей для сервиса анализа.
 * <p>
 * Отвечает за настройку параметров подключения к Kafka и создание объектов свойств для разных топиков.
 */
@Configuration
public class KafkaConsumerConfig {
    /**
     * Настройки потребителя для топика пользовательских действий.
     */
    @Autowired
    private KafkaConsumerSettings userActionConsumerSettings;

    /**
     * Настройки потребителя для топика схожести мероприятий.
     */
    @Autowired
    private KafkaConsumerSettings eventSimilarityConsumerSettings;

    /**
     * Создаёт и возвращает свойства Kafka-потребителя для топика пользовательских действий.
     *
     * @return настроенные свойства Kafka
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.consumer-user-actions")
    public Properties getUserActionConsumerProperties() {
        return buildConsumerProperties(userActionConsumerSettings);
    }

    /**
     * Создаёт и возвращает свойства Kafka-потребителя для топика схожести мероприятий.
     *
     * @return настроенные свойства Kafka
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.consumer-events-similarity")
    public Properties getEventSimilarityConsumerProperties() {
        return buildConsumerProperties(eventSimilarityConsumerSettings);
    }

    /**
     * Строит объект свойств Kafka-потребителя на основе настроек.
     *
     * @param settings настройки потребителя
     * @return готовые свойства для Kafka
     */
    private Properties buildConsumerProperties(KafkaConsumerSettings settings) {
        Properties properties = new Properties();
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG, settings.getClientId());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, settings.getGroupId());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapServers());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, settings.getKeyDeserializer());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, settings.getValueDeserializer());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG, settings.getMaxPollRecords());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_BYTES_CONFIG, settings.getFetchMaxBytes());
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, settings.getMaxPartitionFetchBytes());
        return properties;
    }
}