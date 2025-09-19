package ru.practicum.recomm.aggregator.service;

import ru.practicum.recommendations.avro.UserActionAvro;

/**
 * Интерфейс AggregatorService определяет методы для обработки пользовательских действий.
 * <p>
 * Используется в сервисе агрегации данных для анализа и вычисления схожести между мероприятиями.
 */
public interface AggregatorService {

    /**
     * Обрабатывает новое действие пользователя с мероприятием.
     * <p>
     * Метод принимает объект {@link UserActionAvro}, содержащий информацию о пользователе, мероприятии,
     * типе действия и времени взаимодействия. Данные используются для обновления весовых коэффициентов
     * и расчёта схожести между мероприятиями.
     *
     * @param actionAvro объект, содержащий данные о пользовательском действии в формате Avro
     */
    void processAction(UserActionAvro actionAvro);
}