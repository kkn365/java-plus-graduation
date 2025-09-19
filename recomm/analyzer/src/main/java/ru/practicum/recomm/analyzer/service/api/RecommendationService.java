package ru.practicum.recomm.analyzer.service.api;

import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;

import java.util.List;

/**
 * Интерфейс сервиса для предоставления рекомендаций и анализа событий.
 * <p>
 * Содержит методы, позволяющие получить:
 * - список мероприятий, похожих на указанное,
 * - список рекомендованных мероприятий для конкретного пользователя,
 * - количество взаимодействий с заданными мероприятиями.
 */
public interface RecommendationService {

    /**
     * Возвращает список мероприятий, похожих на указанное.
     * <p>
     * Метод принимает запрос {@link SimilarEventsRequest}, в котором задано идентификатор
     * исходного мероприятия, идентификатор пользователя и максимальное количество результатов.
     *
     * @param similarEventsRequest объект запроса на получение похожих мероприятий
     * @return список объектов {@link RecommendedEvent}, содержащих идентификаторы мероприятий
     *         и оценки их схожести
     */
    List<RecommendedEvent> getSimilarEvents(SimilarEventsRequest similarEventsRequest);

    /**
     * Возвращает список рекомендуемых мероприятий для указанного пользователя.
     * <p>
     * Метод принимает запрос {@link UserPredictionsRequest}, в котором задан идентификатор
     * пользователя и максимальное количество рекомендаций.
     *
     * @param userPredictionsRequest объект запроса на получение рекомендаций
     * @return список объектов {@link RecommendedEvent}, содержащих идентификаторы мероприятий
     *         и оценки их релевантности
     */
    List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest userPredictionsRequest);

    /**
     * Возвращает список количества взаимодействий с указанными мероприятиями.
     * <p>
     * Метод принимает запрос {@link InteractionsCountRequest}, в котором задан список
     * идентификаторов мероприятий.
     *
     * @param interactionsCountRequest объект запроса на подсчёт взаимодействий
     * @return список объектов {@link RecommendedEvent}, где поле event_id — это идентификатор
     *         мероприятия, а score — количество взаимодействий с ним
     */
    List<RecommendedEvent> getInteractionsCount(InteractionsCountRequest interactionsCountRequest);
}