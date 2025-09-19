package ru.practicum.recomm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.recomm.aggregator.service.AggregatorService;
import ru.practicum.recommendations.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class KafkaClient {
    private final AggregatorService aggregatorService;

    @KafkaListener(topics = "${collector.kafka.consumer.topics.user-actions}")
    public void listenActions(UserActionAvro actionAvro) {
        aggregatorService.processAction(actionAvro);
    }
}