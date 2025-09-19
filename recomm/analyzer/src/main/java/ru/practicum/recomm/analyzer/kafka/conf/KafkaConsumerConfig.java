package ru.practicum.recomm.analyzer.kafka.conf;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.recomm.kafka.deserializer.EventSimilarityDeserializer;
import ru.practicum.recomm.kafka.deserializer.UserActionDeserializer;
import ru.practicum.recommendations.avro.EventSimilarityAvro;
import ru.practicum.recommendations.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурационный класс настройки Kafka-потребителей для работы с разными десериализаторами.
 * <p>
 * В данном классе создаются отдельные фабрики потребителей (ConsumerFactory) и контейнеры прослушивания
 * (KafkaListenerContainerFactory), чтобы использовать разные десериализаторы для разных топиков.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Создаёт ConsumerFactory для потребителя, который работает с топиком пользовательских действий.
     * <p>
     * Используется десериализатор {@link UserActionDeserializer}, чтобы преобразовывать сообщения из байтов
     * в объекты типа {@link UserActionAvro}.
     *
     * @return готовая ConsumerFactory для топика пользовательских действий
     */
    @Bean
    public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "recomm.analyzer.consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Создаёт ConcurrentKafkaListenerContainerFactory для топика пользовательских действий.
     * <p>
     * Используется ранее созданная ConsumerFactory, чтобы обеспечить правильную обработку сообщений.
     *
     * @return готовый KafkaListenerContainerFactory для топика пользовательских действий
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userActionConsumerFactory());
        return factory;
    }

    /**
     * Создаёт ConsumerFactory для потребителя, который работает с топиком коэффициентов схожести.
     * <p>
     * Используется десериализатор {@link EventSimilarityDeserializer}, чтобы преобразовывать сообщения из байтов
     * в объекты типа {@link EventSimilarityAvro}.
     *
     * @return готовая ConsumerFactory для топика коэффициентов схожести
     */
    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> similarityConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "recomm.analyzer.consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Создаёт ConcurrentKafkaListenerContainerFactory для топика коэффициентов схожести.
     * <p>
     * Используется ранее созданная ConsumerFactory, чтобы обеспечить правильную обработку сообщений.
     *
     * @return готовый KafkaListenerContainerFactory для топика коэффициентов схожести
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> similarityListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(similarityConsumerFactory());
        return factory;
    }
}