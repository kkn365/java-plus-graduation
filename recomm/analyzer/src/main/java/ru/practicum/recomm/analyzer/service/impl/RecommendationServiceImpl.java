package ru.practicum.recomm.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.recomm.analyzer.model.EventSimilarity;
import ru.practicum.recomm.analyzer.model.UserAction;
import ru.practicum.recomm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.recomm.analyzer.repository.UserActionRepository;
import ru.practicum.recomm.analyzer.service.api.RecommendationService;
import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    /**
     * Метод генерирует рекомендации мероприятий для указанного пользователя.
     * <p>
     * Алгоритм работает следующим образом:
     * 1. Получает последние N взаимодействий пользователя с мероприятиями.
     * 2. На основе этих событий формирует список идентификаторов, с которыми пользователь уже взаимодействовал.
     * 3. Ищет события, похожие на те, с которыми взаимодействовал пользователь.
     * 4. Выбирает уникальные непросмотренные мероприятия.
     * 5. Рассчитывает взвешенные оценки для каждого кандидата на рекомендацию.
     * 6. Возвращает отсортированный список рекомендаций.
     *
     * @param request объект запроса, содержащий идентификатор пользователя и максимальное количество рекомендаций
     * @return        список рекомендаций {@link RecommendedEvent}, отсортированный по убыванию оценки релевантности
     */
    @Override
    public List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest request) {
        log.info("Генерация рекомендаций для пользователя {}", request.getUserId());

        long userId = request.getUserId();
        long maxResults = request.getMaxResults(); // Всегда возвращает long (в proto3 0, если не задано)
        if (maxResults <= 0) {
            maxResults = 10;
        }

        // Получаем последние N взаимодействий пользователя
        List<UserAction> recentActions = userActionRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(UserAction::getInteractAt).reversed())
                .limit(maxResults)
                .toList();

        if (recentActions.isEmpty()) {
            return Collections.emptyList();
        }

        // Формируем список идентификаторов событий, с которыми взаимодействовал пользователь
        Set<Long> userInteractedEvents = recentActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        // Находим все события, похожие на те, с которыми взаимодействовал пользователь
        List<EventSimilarity> similarEvents = eventSimilarityRepository
                .findAllBySourceEventIdInOrTargetEventIdIn(userInteractedEvents, userInteractedEvents);

        // Выбираем уникальные непросмотренные мероприятия
        Set<Long> recommendedEvents = similarEvents.stream()
                .flatMap(es -> Stream.of(es.getSourceEventId(), es.getTargetEventId()))
                .filter(eventId -> !userInteractedEvents.contains(eventId))
                .distinct()
                .limit(maxResults)
                .collect(Collectors.toSet());

        // Рассчитываем взвешенные оценки
        Map<Long, Double> weightedScores = new HashMap<>();
        for (EventSimilarity similarity : similarEvents) {
            for (Long baseEventId : userInteractedEvents) {
                Long candidateEventId = getOtherEvent(similarity, baseEventId);

                if (candidateEventId == null || userInteractedEvents.contains(candidateEventId)) {
                    continue;
                }

                double userScore = getUserScore(baseEventId);
                double similarityScore = similarity.getSimilarityScore();
                weightedScores.put(candidateEventId,
                        weightedScores.getOrDefault(candidateEventId, 0.0) + (userScore * similarityScore));
            }
        }

        // Сортируем и формируем ответ
        return weightedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> RecommendedEvent.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    /**
     * Метод возвращает список мероприятий, похожих на указанное.
     * <p>
     * На основе запроса {@link SimilarEventsRequest} метод получает идентификатор мероприятия,
     * идентификатор пользователя и максимальное количество результатов. Затем он ищет все события,
     * имеющие схожесть с указанным, фильтрует те, которые пользователь ещё не просматривал,
     * и возвращает отсортированный список рекомендаций.
     *
     * @param request объект запроса, содержащий идентификатор мероприятия, идентификатор пользователя
     *                и максимальное количество результатов
     * @return        список рекомендуемых мероприятий, отсортированный по убыванию степени схожести
     */
    @Override
    public List<RecommendedEvent> getSimilarEvents(SimilarEventsRequest request) {
        log.info("Поиск похожих мероприятий для события {}", request.getEventId());

        long eventId = request.getEventId();
        long userId = request.getUserId();
        long maxResults = request.getMaxResults(); // Всегда возвращает long (в proto3 0, если не задано)
        if (maxResults <= 0) {
            maxResults = 10;
        }

        // Получаем список всех похожих событий
        List<EventSimilarity> allSimilarities = eventSimilarityRepository
                .findAllBySourceEventIdOrTargetEventId(eventId, eventId);

        // Получаем список событий, которые пользователь уже просматривал
        Set<Long> viewedEvents = userActionRepository.findByUserId(userId)
                .stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        // Фильтруем только непросмотренные события
        List<RecommendedEvent> result = allSimilarities.stream()
                .filter(similarity -> {
                    Long otherEvent = getOtherEvent(similarity, eventId);
                    return otherEvent != null && !viewedEvents.contains(otherEvent);
                })
                .map(similarity -> {
                    Long otherEvent = getOtherEvent(similarity, eventId);
                    return RecommendedEvent.newBuilder()
                            .setEventId(otherEvent)
                            .setScore(similarity.getSimilarityScore())
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .limit(maxResults)
                .toList();

        log.info("Найдено {} похожих мероприятий", result.size());
        return result;
    }

    /**
     * Метод подсчитывает количество взаимодействий с заданными мероприятиями.
     * <p>
     * Принимает запрос {@link InteractionsCountRequest}, в котором указан список идентификаторов мероприятий.
     * Для каждого мероприятия из списка вычисляется сумма весов всех пользовательских действий (например, лайков, просмотров).
     * Результат возвращается в виде списка объектов {@link RecommendedEvent}, где для каждого мероприятия указывается
     * идентификатор и суммарный вес взаимодействий. Список сортируется по убыванию веса.
     *
     * @param request объект запроса, содержащий список идентификаторов мероприятий
     * @return        отсортированный список рекомендаций с количеством взаимодействий для каждого мероприятия
     */
    @Override
    public List<RecommendedEvent> getInteractionsCount(InteractionsCountRequest request) {
        log.info("Подсчёт взаимодействий для событий: {}", request.getEventIdsList());

        Map<Long, Double> interactionCounts = userActionRepository.findByEventIdIn(request.getEventIdsList())
                .stream()
                .collect(Collectors.groupingBy(
                        UserAction::getEventId,
                        Collectors.summingDouble(UserAction::getScore)
                ));

        return interactionCounts.entrySet().stream()
                .map(entry -> RecommendedEvent.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Возвращает идентификатор события, отличного от заданного.
     * <p>
     * Метод принимает объект EventSimilarity, представляющий пару схожих мероприятий, и идентификатор одного из них.
     * Возвращает идентификатор второго мероприятия в паре. Если ни одно из полей не совпадает с baseEventId,
     * возвращается null.
     *
     * @param similarity   объект, описывающий схожесть между двумя мероприятиями
     * @param baseEventId  идентификатор одного из мероприятий в паре
     * @return             идентификатор другого мероприятия или null, если не найдено
     */
    private Long getOtherEvent(EventSimilarity similarity, Long baseEventId) {
        if (similarity.getSourceEventId().equals(baseEventId)) {
            return similarity.getTargetEventId();
        } else if (similarity.getTargetEventId().equals(baseEventId)) {
            return similarity.getSourceEventId();
        }
        return null;
    }

    /**
     * Возвращает среднюю оценку (вес) взаимодействий с заданным мероприятием.
     * <p>
     * Если для мероприятия не найдено никаких действий, возвращается значение по умолчанию — 1.0.
     *
     * @param eventId идентификатор мероприятия, для которого нужно рассчитать среднюю оценку
     * @return среднее значение веса (score), если действия найдены, иначе 1.0
     */
    private double getUserScore(Long eventId) {
        return userActionRepository.findByEventId(eventId)
                .stream()
                .mapToDouble(UserAction::getScore)
                .average()
                .orElse(1.0);
    }
}