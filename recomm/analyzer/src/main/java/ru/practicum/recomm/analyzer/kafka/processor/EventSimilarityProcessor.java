package ru.practicum.recomm.analyzer.kafka.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.practicum.recomm.analyzer.service.api.EventSimilarityService;
import ru.practicum.recommendations.avro.EventSimilarityAvro;

/**
 * Обработчик событий схожести между мероприятиями из Kafka.
 * <p>
 * Использует аннотацию @KafkaListener для подписки на топик схожести мероприятий.
 * Обрабатывает каждую запись, вызывая соответствующий сервис, и подтверждает получение сообщения.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityProcessor {

    private final EventSimilarityService eventSimilarityService;

    /**
     * Обрабатывает сообщения из топика схожести мероприятий.
     * <p>
     * Каждое сообщение преобразуется в объект EventSimilarityAvro и передаётся в сервис для дальнейшей обработки.
     * После успешной обработки подтверждается получение сообщения.
     *
     * @param record        сообщение из Kafka
     * @param acknowledgment механизм подтверждения получения сообщения
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.events-similarity}",
            groupId = "${spring.kafka.consumer-events-similarity.group-id}"
    )
    public void consume(ConsumerRecord<String, EventSimilarityAvro> record, Acknowledgment acknowledgment) {
        try {
            log.debug("Обработка сообщения из топика {}: партиция={}, смещение={}",
                    record.topic(), record.partition(), record.offset());

            eventSimilarityService.handleEventSimilarity(record.value());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", record.value(), e);
            // Добавить логику повторной обработки или отправки в dead-letter topic при необходимости
        }
    }
}