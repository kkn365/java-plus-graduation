package ru.practicum.recomm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.recomm.aggregator.service.AggregatorService;
import ru.practicum.recommendations.avro.UserActionAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaClient {
    private final AggregatorService aggregatorService;

    @KafkaListener(
            topics = "${kafka.topic.stats.v1}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenActions(UserActionAvro actionAvro) {
        aggregatorService.processAction(actionAvro);
    }
}