package ru.practicum.recomm.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.recomm.analyzer.model.EventSimilarity;
import ru.practicum.recomm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.recomm.analyzer.service.api.EventSimilarityService;
import ru.practicum.recommendations.avro.EventSimilarityAvro;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Реализация сервиса EventSimilarityService для обработки событий схожести между мероприятиями.
 * <p>
 * Обрабатывает Avro-объекты, преобразует их в сущности JPA и сохраняет в репозиторий.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;

    /**
     * Обрабатывает событие схожести между двумя мероприятиями.
     * <p>
     * Метод принимает Avro-объект, преобразует его в сущность EventSimilarity,
     * проверяет наличие записи в БД и либо обновляет, либо создаёт новую запись.
     *
     * @param eventSimilarityAvro Avro-объект с данными о схожести
     */
    @Override
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Обработка события схожести: {}", eventSimilarityAvro);

        // MapStruct отказывается корректно работать с временными метками в Avro
        EventSimilarity eventSimilarity = EventSimilarity.builder()
                .sourceEventId(eventSimilarityAvro.getEventA())
                .targetEventId(eventSimilarityAvro.getEventB())
                .similarityScore(eventSimilarityAvro.getScore())
                .calculatedAt(LocalDateTime.from(eventSimilarityAvro.getTimestamp()))
                .build();

        Optional<EventSimilarity> existing = eventSimilarityRepository
                .findBySourceEventIdAndTargetEventId(
                        eventSimilarity.getSourceEventId(),
                        eventSimilarity.getTargetEventId()
                );

        if (existing.isPresent()) {
            log.debug("Обновление существующей записи схожести для мероприятий {} и {}",
                    eventSimilarity.getSourceEventId(), eventSimilarity.getTargetEventId());
            EventSimilarity updated = existing.get();
            updated.setSimilarityScore(eventSimilarity.getSimilarityScore());
            updated.setCalculatedAt(eventSimilarity.getCalculatedAt());
            eventSimilarityRepository.save(updated);
        } else {
            log.debug("Создание новой записи схожести для мероприятий {} и {}",
                    eventSimilarity.getSourceEventId(), eventSimilarity.getTargetEventId());
            eventSimilarityRepository.save(eventSimilarity);
        }
    }
}