package ru.practicum.recomm.client;

import com.google.common.collect.Lists;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;
import ru.practicum.recommendations.services.RecommendationsControllerGrpc;

import java.util.List;

/**
 * Клиент для взаимодействия с gRPC-сервисом Analyzer.
 * <p>
 * Предоставляет методы для получения рекомендаций, похожих мероприятий и подсчёта взаимодействий.
 */
@Component
public class AnalyzerClient {

    /**
     * Стаб gRPC-клиента для работы с сервисом {@link RecommendationsControllerGrpc}.
     * <p>
     * Адрес сервиса указывается через параметр конфигурации "analyzer".
     */
    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzer;

    /**
     * Получает список рекомендуемых мероприятий для указанного пользователя.
     *
     * @param request объект запроса {@link UserPredictionsRequest}, содержащий идентификатор пользователя
     *                и максимальное количество рекомендаций
     * @return список объектов {@link RecommendedEvent}, каждый из которых содержит идентификатор мероприятия
     *         и оценку его релевантности
     */
    public List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest request) {
        return Lists.newArrayList(analyzer.getRecommendationsForUser(request));
    }

    /**
     * Получает список мероприятий, похожих на указанное.
     *
     * @param request объект запроса {@link SimilarEventsRequest}, содержащий идентификатор мероприятия,
     *                идентификатор пользователя и максимальное количество результатов
     * @return список объектов {@link RecommendedEvent}, каждый из которых содержит идентификатор похожего мероприятия
     *         и оценку сходства
     */
    public List<RecommendedEvent> getSimilarEvent(SimilarEventsRequest request) {
        return Lists.newArrayList(analyzer.getSimilarEvents(request));
    }

    /**
     * Получает список количества взаимодействий с указанными мероприятиями.
     *
     * @param request объект запроса {@link InteractionsCountRequest}, содержащий список идентификаторов мероприятий
     * @return список объектов {@link RecommendedEvent}, где event_id — это идентификатор мероприятия,
     *         а score — количество взаимодействий с ним
     */
    public List<RecommendedEvent> getInteractionsCount(InteractionsCountRequest request) {
        return Lists.newArrayList(analyzer.getInteractionsCount(request));
    }
}