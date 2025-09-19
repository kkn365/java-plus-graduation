package ru.practicum.recomm.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.recommendations.avro.ActionTypeAvro;
import ru.practicum.recommendations.avro.EventSimilarityAvro;
import ru.practicum.recommendations.avro.UserActionAvro;
import ru.practicum.recomm.aggregator.kafka.config.KafkaTopics;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса AggregatorService для обработки пользовательских действий и вычисления схожести между мероприятиями.
 * <p>
 * Служит для анализа данных о взаимодействии пользователей с мероприятиями, обновления весовых коэффициентов и
 * отправки информации о схожести в Kafka-топик.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    /**
     * Шаблон Kafka для отправки сообщений в формате Avro.
     */
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    /**
     * Настройки имен Kafka-топиков.
     */
    private final KafkaTopics kafkaTopics;

    // === Матрица весов действий пользователей c мероприятиями ===

    /**
     * Хранилище весов взаимодействий между пользователями и мероприятиями.
     * Ключ: id мероприятия, Значение: Map<id пользователя, взаимодействие с максимальным весом>.
     */
    private final Map<Long, Map<Long, Double>> weightedUserActionsMatrix = new ConcurrentHashMap<>();

    // === Хранилища частных сумм ===

    /**
     * 1. Хранилище общих сумм весов каждого из мероприятий.
     * Ключ: id мероприятия, Значение: сумма весов действий пользователей с ним.
     */
    private final Map<Long, Double> totalWeights = new ConcurrentHashMap<>();

    /**
     * 2. Хранилище минимальных сумм весов для каждой пары мероприятий.
     * Ключ: id мероприятия A, Значение: Map<id мероприятия B, сумма их минимальных весов>.
     * Используется для расчёта схожести между событиями.
     */
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    /**
     * Реализация метода обработки пользовательского действия.
     * <p>
     * При получении действия пользователя, рассчитывает коэффициенты схожести между мероприятиями,
     * основываясь на новом весе взаимодействия. Если были рассчитаны новые коэффициенты — отправляет их в Kafka.
     *
     * @param actionAvro объект, содержащий данные о пользовательском действии (ID пользователя, ID мероприятия, тип действия)
     */
    @Override
    public void processAction(UserActionAvro actionAvro) {
        log.info("Получено действие пользователя: {}", actionAvro);
        List<EventSimilarityAvro> similarityList = updateSimilarities(actionAvro);
        if (!similarityList.isEmpty()) {
            log.info("Было рассчитано {} коэффициентов схожести", similarityList.size());
            sendSimilarities(similarityList);
        }
    }

    /**
     * Обновляет коэффициенты схожести между мероприятиями после получения нового пользовательского действия.
     * <p>
     * Метод проверяет, является ли это первым взаимодействием пользователя с мероприятием. Если да —
     * рассчитывает новые коэффициенты схожести. В противном случае сравнивает старый и новый вес взаимодействия
     * и при необходимости пересчитывает коэффициенты схожести для всех других событий.
     *
     * @param actionAvro объект, содержащий данные о пользовательском действии:
     *                   - userId — идентификатор пользователя
     *                   - eventId — идентификатор мероприятия
     *                   - actionType — тип действия (VIEW, REGISTER, LIKE и т.д.)
     * @return список объектов EventSimilarityAvro — коэффициенты схожести, рассчитанные для данного события
     */
    public List<EventSimilarityAvro> updateSimilarities(UserActionAvro actionAvro) {
        Double weight = getActionWeight(actionAvro.getActionType());
        Long userId = actionAvro.getUserId();
        Long eventId = actionAvro.getEventId();

        if (!weightedUserActionsMatrix.containsKey(eventId)) {
            log.info("Это первое взаимодействие с мероприятием ID={}", eventId);
            return calculateNewSimilarities(eventId, userId, weight);
        }

        Map<Long, Double> itemWeights = weightedUserActionsMatrix.get(eventId);
        double oldWeight = itemWeights.getOrDefault(userId, 0.0);
        double newWeight = Math.max(oldWeight, weight);

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        if (newWeight != oldWeight) {
            log.info("Получена новая оценка '{}' пользователя ID={} для мероприятия ID={}", weight, userId, eventId);
            itemWeights.put(userId, newWeight);
            double eventAWeightDelta = newWeight - oldWeight;
            totalWeights.merge(eventId, eventAWeightDelta, Double::sum);

            for (Long otherEventId : weightedUserActionsMatrix.keySet()) {
                if (otherEventId.equals(eventId)) {
                    continue;
                }

                Optional<EventSimilarityAvro> similarity = updateSums(userId, eventId, otherEventId, oldWeight, newWeight);
                similarity.ifPresent(similarities::add);
            }
        }
        return similarities;
    }

    /**
     * Обновляет сумму минимальных весов между двумя мероприятиями и вычисляет коэффициент схожести.
     * <p>
     * Метод учитывает изменение веса взаимодействия пользователя с первым мероприятием (eventA)
     * и пересчитывает коэффициент схожести между eventA и другим мероприятием (eventB) по формуле
     * косинусного сходства. Если пользователь не взаимодействовал с eventB, возвращает пустой Optional.
     *
     * @param userId             идентификатор пользователя
     * @param eventA             идентификатор первого мероприятия
     * @param eventB             идентификатор второго мероприятия
     * @param eventAOldWeight    старый вес взаимодействия пользователя с eventA
     * @param eventANewWeight    новый вес взаимодействия пользователя с eventA
     * @return                   Optional<EventSimilarityAvro> — результат расчёта схожести,
     *                           если он был успешно вычислен
     */
    private Optional<EventSimilarityAvro> updateSums(
            Long userId,
            Long eventA,
            Long eventB,
            Double eventAOldWeight,
            Double eventANewWeight
    ) {
        Double eventBWeight = weightedUserActionsMatrix.get(eventB).getOrDefault(userId, 0.0);
        if (eventBWeight == 0.0) {
            // Пользователь не взаимодействовал с мероприятием
            return Optional.empty();
        } else {
            // Рассчитываем изменение минимального веса между событиями A и B
            double oldMinAB = Math.min(eventAOldWeight, eventBWeight);
            double newMinAB = Math.min(eventANewWeight, eventBWeight);
            double minABDelta = newMinAB - oldMinAB;

            // Обновляем сумму минимальных весов для пары событий
            double updatedMinWeightsSum = get(eventA, eventB) + minABDelta;
            put(eventA, eventB, updatedMinWeightsSum);

            // Получаем обновлённую сумму минимальных весов
            double minWeightsSum = get(eventA, eventB);

            // Вычисляем нормы (корни из сумм квадратов весов)
            double norm1 = Math.sqrt(totalWeights.getOrDefault(eventA, 0.0));
            double norm2 = Math.sqrt(totalWeights.getOrDefault(eventB, 0.0));

            if (norm1 == 0 || norm2 == 0) {
                return Optional.empty();
            }

            // Вычисляем схожесть по формуле косинусного сходства
            double similarity = minWeightsSum / (norm1 * norm2);

            // Возвращаем результат в виде Avro-объекта
            return Optional.of(createSimilarityAvro(eventA, eventB, similarity));
        }
    }

    /**
     * Рассчитывает коэффициенты схожести между новым мероприятием и всеми остальными мероприятиями,
     * на основе первого взаимодействия пользователя.
     * <p>
     * Метод добавляет новое мероприятие в матрицу весов, обновляет общую сумму весов,
     * и для каждого существующего мероприятия рассчитывает коэффициент схожести по формуле косинусного сходства.
     *
     * @param eventA   идентификатор нового мероприятия
     * @param user     идентификатор пользователя, который совершил действие
     * @param weightA  вес взаимодействия пользователя с новым мероприятием
     * @return         список объектов EventSimilarityAvro — результаты расчёта коэффициентов схожести
     */
    private List<EventSimilarityAvro> calculateNewSimilarities(Long eventA, Long user, Double weightA) {
        weightedUserActionsMatrix.computeIfAbsent(eventA, k -> new HashMap<>(Map.of(user, weightA)));
        totalWeights.put(eventA, weightA);
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : weightedUserActionsMatrix.entrySet()) {
            Long eventB = entry.getKey();
            if (eventB.equals(eventA)) {
                continue; // Пропускаем сравнение с самим собой
            }

            Double weightB = entry.getValue().getOrDefault(user, 0.0);
            if (weightB == 0.0) {
                continue; // Пропускаем события без взаимодействия пользователя
            }

            double minWeight = Math.min(weightA, weightB);
            put(eventA, eventB, minWeight);

            double norm1 = totalWeights.getOrDefault(eventA, 0.0);
            double norm2 = totalWeights.getOrDefault(eventB, 0.0);

            if (norm1 == 0 || norm2 == 0) {
                continue; // Избегаем деления на ноль
            }

            double similarity = minWeight / (Math.sqrt(norm1) * Math.sqrt(norm2));

            similarities.add(createSimilarityAvro(eventA, eventB, similarity));
        }
        return similarities;
    }

    /**
     * Возвращает сумму минимальных весов для пары мероприятий.
     *
     * @param eventA  идентификатор первого мероприятия
     * @param eventB  идентификатор второго мероприятия
     * @return значение схожести
     */
    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }

    /**
     * Устанавливает значение суммы минимальных весов для пары мероприятий.
     *
     * @param eventA  идентификатор первого мероприятия
     * @param eventB  идентификатор второго мероприятия
     * @param sum     значение схожести
     */
    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    /**
     * Возвращает вес взаимодействия в зависимости от типа действия.
     *
     * @param type тип действия
     * @return значение рейтинга
     * @throws IllegalArgumentException если передан неизвестный тип действия
     */
    private double getActionWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + type);
        };
    }

    /**
     * Создаёт объект EventSimilarityAvro, представляющий коэффициент схожести между двумя мероприятиями.
     * <p>
     * Метод гарантирует, что идентификатор события A всегда меньше или равен идентификатору события B,
     * чтобы избежать дублирования записей для одной и той же пары событий в обратном порядке.
     *
     * @param eventA           идентификатор первого мероприятия
     * @param eventB           идентификатор второго мероприятия
     * @param similarityScore  значение коэффициента схожести между мероприятиями (от 0 до 1)
     * @return                 объект EventSimilarityAvro, содержащий информацию о схожести
     */
    private EventSimilarityAvro createSimilarityAvro(Long eventA, Long eventB, double similarityScore) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(similarityScore)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Отправляет список коэффициентов схожести между мероприятиями в Kafka.
     * <p>
     * Метод перебирает все элементы списка и отправляет каждый из них в соответствующий топик Kafka.
     * В случае успешной отправки логируется информация о сообщении. При ошибке — логируется исключение.
     *
     * @param similarityList список объектов EventSimilarityAvro, представляющих коэффициенты схожести
     */
    private void sendSimilarities(List<EventSimilarityAvro> similarityList) {
        for (EventSimilarityAvro similarity : similarityList) {
            try {
                kafkaTemplate.send(kafkaTopics.getEventsSimilarity(), similarity);
                log.info("Отправлено в Kafka: {}", similarity);
            } catch (Exception e) {
                log.error("Ошибка при отправке в Kafka: {}", similarity, e);
            }
        }
    }
}