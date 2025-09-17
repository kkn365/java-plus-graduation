package ru.practicum.recomm.analyzer.service.api;

import ru.practicum.recommendations.avro.EventSimilarityAvro;

/**
 * Интерфейс сервиса для обработки событий схожести между мероприятиями.
 * <p>
 * Предоставляет методы для приема и обработки данных о схожести двух мероприятий,
 * полученных из Kafka в формате Avro.
 */
public interface EventSimilarityService {

    /**
     * Обрабатывает событие схожести между двумя мероприятиями.
     * <p>
     * Метод принимает объект {@link EventSimilarityAvro}, который содержит информацию о двух мероприятиях
     * и степени их схожести. Данные могут использоваться для дальнейшего анализа или хранения.
     *
     * @param eventSimilarityAvro объект, содержащий данные о схожести мероприятий
     */
    void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro);
}