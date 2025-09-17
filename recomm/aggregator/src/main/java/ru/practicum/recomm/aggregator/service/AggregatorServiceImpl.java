package ru.practicum.recomm.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.recomm.aggregator.model.EventSimilarity;
import ru.practicum.recommendations.avro.ActionTypeAvro;
import ru.practicum.recommendations.avro.EventSimilarityAvro;
import ru.practicum.recommendations.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация сервиса AggregatorService для обработки пользовательских действий и вычисления схожести между мероприятиями.
 * <p>
 * Служит для анализа данных о взаимодействии пользователей с мероприятиями, обновления весовых коэффициентов и
 * отправки информации о схожести в Kafka-топик.
 */
@Slf4j
@Service
public class AggregatorServiceImpl implements AggregatorService {

    /**
     * Хранилище весов взаимодействий между пользователями и мероприятиями.
     * Ключ: id мероприятия, Значение: Map<id пользователя, вес взаимодействия>.
     */
    private final Map<Long, Map<Long, Double>> weightMap = new ConcurrentHashMap<>();

    /**
     * Хранилище сумм весов по каждому мероприятию.
     * Ключ: id мероприятия, Значение: сумма весов всех пользователей.
     */
    private final Map<Long, Double> weightSumMap = new ConcurrentHashMap<>();

    /**
     * Хранилище минимальных сумм весов для пар мероприятий.
     * Используется для расчёта схожести между событиями.
     */
    private final Map<Long, Map<Long, Double>> eventSimilarityScores = new ConcurrentHashMap<>();

    /**
     * Хранилище событий, с которыми взаимодействовал каждый пользователь.
     * Ключ: id пользователя, Значение: множество id мероприятий.
     */
    private final Map<Long, Set<Long>> userEvents = new ConcurrentHashMap<>();

    /**
     * Шаблон Kafka для отправки сообщений в формате Avro.
     */
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    /**
     * Конструктор класса.
     *
     * @param kafkaTemplate шаблон Kafka для отправки сообщений
     */
    public AggregatorServiceImpl(KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Обрабатывает новое действие пользователя с мероприятием.
     * <p>
     * Выполняет проверку наличия события у пользователя, определяет новый рейтинг и вызывает соответствующую логику.
     *
     * @param actionAvro объект с данными о пользовательском действии в формате Avro
     */
    @Override
    public void processAction(UserActionAvro actionAvro) {
        long userId = actionAvro.getUserId();
        long eventId = actionAvro.getEventId();
        double newRating = getActionRating(actionAvro.getActionType());

        log.debug("Обработка действия пользователя {} с мероприятием {}", userId, eventId);

        userEvents.computeIfAbsent(userId, k -> new HashSet<>());
        if (!userEvents.get(userId).contains(eventId)) {
            handleNewEvent(userId, eventId, newRating);
        } else {
            handleExistingEvent(userId, eventId, newRating);
        }
    }

    /**
     * Обрабатывает новое взаимодействие пользователя с мероприятием.
     * <p>
     * Обновляет веса, суммы весов и события пользователя, а также запускает обновление схожести.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор мероприятия
     * @param newRating   новый рейтинг взаимодействия
     */
    private void handleNewEvent(long userId, long eventId, double newRating) {
        log.info("Новое взаимодействие пользователя {} с мероприятием {}", userId, eventId);

        weightSumMap.compute(eventId, (k, v) -> v == null ? newRating : v + newRating);
        weightMap.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>())
                .put(userId, newRating);

        userEvents.get(userId).add(eventId);

        updateSimilarities(userId, eventId, newRating);
    }

    /**
     * Обрабатывает уже существующее взаимодействие пользователя с мероприятием.
     * <p>
     * Если новый рейтинг выше предыдущего, обновляет веса и суммы, а также запускает обновление схожести.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор мероприятия
     * @param newRating   новый рейтинг взаимодействия
     */
    private void handleExistingEvent(long userId, long eventId, double newRating) {
        Map<Long, Double> eventUsers = weightMap.get(eventId);
        double oldRating = eventUsers.get(userId);

        if (newRating > oldRating) {
            double delta = newRating - oldRating;
            weightSumMap.put(eventId, weightSumMap.get(eventId) + delta);
            eventUsers.replace(userId, newRating);

            updateSimilarities(userId, eventId, newRating);
        }
    }

    /**
     * Обновляет схожесть между текущим мероприятием и всеми другими, с которыми взаимодействовал пользователь.
     * <p>
     * Вызывает методы для вычисления и отправки результатов.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор мероприятия
     * @param rating      рейтинг взаимодействия
     */
    private void updateSimilarities(long userId, long eventId, double rating) {
        Set<Long> relatedEvents = getRelatedEvents(userId, eventId);

        for (Long relatedEvent : relatedEvents) {
            calculateAndUpdateSimilarity(eventId, relatedEvent, userId, rating);
        }

        sendSimilarities(eventId, userId);
    }

    /**
     * Получает список всех мероприятий, с которыми взаимодействовал пользователь, кроме текущего.
     *
     * @param userId          идентификатор пользователя
     * @param currentEventId  идентификатор текущего мероприятия
     * @return список связанных мероприятий
     */
    private Set<Long> getRelatedEvents(long userId, long currentEventId) {
        return userEvents.get(userId)
                .stream()
                .filter(e -> !e.equals(currentEventId))
                .collect(Collectors.toSet());
    }

    /**
     * Вычисляет и обновляет схожесть между двумя мероприятиями на основе нового рейтинга.
     *
     * @param eventA      идентификатор первого мероприятия
     * @param eventB      идентификатор второго мероприятия
     * @param userId      идентификатор пользователя
     * @param newRating   новый рейтинг взаимодействия
     */
    private void calculateAndUpdateSimilarity(long eventA, long eventB, long userId, double newRating) {
        double oldMin = Math.min(weightMap.get(eventA).get(userId), weightMap.get(eventB).get(userId));
        double newMin = Math.min(newRating, weightMap.get(eventB).get(userId));
        double delta = newMin - oldMin;

        if (delta > 0) {
            put(eventA, eventB, get(eventA, eventB) + delta);
        }
    }

    /**
     * Отправляет информацию о схожести мероприятий в Kafka-топик.
     *
     * @param eventId     идентификатор мероприятия
     * @param userId      идентификатор пользователя
     */
    private void sendSimilarities(long eventId, long userId) {
        List<EventSimilarity> similarities = calculateSimilarities(eventId, userId);
        Instant timestamp = Instant.now();

        similarities.forEach(event -> {
            try {
                EventSimilarityAvro avroEvent = createAvroEvent(event, timestamp);
                kafkaTemplate.send(String.valueOf(avroEvent.getSourceEventId()), avroEvent);
                log.info("Отправлено в Kafka: {}", avroEvent);
            } catch (Exception e) {
                log.error("Ошибка при отправке в Kafka: {}", event, e);
            }
        });
    }

    /**
     * Вычисляет список схожести текущего мероприятия с другими мероприятиями пользователя.
     *
     * @param eventA  идентификатор текущего мероприятия
     * @param userId  идентификатор пользователя
     * @return список схожести мероприятий
     */
    private List<EventSimilarity> calculateSimilarities(long eventA, long userId) {
        return userEvents.get(userId)
                .stream()
                .filter(eventB -> !eventB.equals(eventA))
                .map(eventB -> {
                    double similarity = get(eventA, eventB) /
                            (Math.sqrt(weightSumMap.get(eventA)) * Math.sqrt(weightSumMap.get(eventB)));

                    return createEventSimilarity(eventA, eventB, similarity);
                })
                .collect(Collectors.toList());
    }

    /**
     * Создаёт объект EventSimilarity из двух идентификаторов мероприятий и значения схожести.
     *
     * @param eventA      идентификатор первого мероприятия
     * @param eventB      идентификатор второго мероприятия
     * @param similarity  значение схожести
     * @return объект EventSimilarity
     */
    private EventSimilarity createEventSimilarity(long eventA, long eventB, double similarity) {
        return new EventSimilarity(
                Math.min(eventA, eventB),
                Math.max(eventA, eventB),
                similarity
        );
    }

    /**
     * Создаёт объект EventSimilarityAvro из EventSimilarity и временной метки.
     *
     * @param similarity  объект EventSimilarity
     * @param timestamp   временная метка
     * @return объект EventSimilarityAvro
     */
    private EventSimilarityAvro createAvroEvent(EventSimilarity similarity, Instant timestamp) {
        return EventSimilarityAvro.newBuilder()
                .setSourceEventId(similarity.sourceEventId())
                .setTargetEventId(similarity.targetEventId())
                .setSimilarityScore(similarity.similarityScore())
                .setCalculatedAt(timestamp)
                .build();
    }

    /**
     * Возвращает текущее значение схожести между двумя мероприятиями.
     *
     * @param eventA  идентификатор первого мероприятия
     * @param eventB  идентификатор второго мероприятия
     * @return значение схожести
     */
    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return eventSimilarityScores
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }

    /**
     * Устанавливает значение схожести между двумя мероприятиями.
     *
     * @param eventA  идентификатор первого мероприятия
     * @param eventB  идентификатор второго мероприятия
     * @param sum     значение схожести
     */
    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        eventSimilarityScores
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    /**
     * Возвращает рейтинг взаимодействия в зависимости от типа действия.
     *
     * @param type тип действия
     * @return значение рейтинга
     * @throws IllegalArgumentException если передан неизвестный тип действия
     */
    private double getActionRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + type);
        };
    }
}