package ru.practicum.recomm.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.recomm.analyzer.service.api.EventSimilarityService;
import ru.practicum.recommendations.avro.EventSimilarityAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventsSimilarityKafkaClient {

    private final EventSimilarityService eventSimilarityService;

    @KafkaListener(
            topics = "${analyzer.kafka.consumer.topics.events-similarity}",
            containerFactory = "similarityListenerContainerFactory"
    )
    public void listenActions(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Получена схожесть: {}", eventSimilarityAvro);
        eventSimilarityService.handleEventSimilarity(eventSimilarityAvro);
    }
}
