package ru.practicum.recomm.analyzer.service.api;

import ru.practicum.recommendations.avro.UserActionAvro;

/**
 * Интерфейс сервиса для обработки действий пользователей.
 * <p>
 * Предоставляет метод для приема и обработки данных о взаимодействии пользователя с мероприятием,
 * полученных из Kafka в формате Avro.
 */
public interface UserActionService {

    /**
     * Обрабатывает действие пользователя с мероприятием.
     * <p>
     * Метод принимает объект {@link UserActionAvro}, который содержит информацию о пользователе,
     * мероприятии, времени взаимодействия и весе (уровне интереса). Данные могут использоваться
     * для дальнейшего анализа, хранения или генерации рекомендаций.
     *
     * @param userActionAvro объект, содержащий данные о действии пользователя
     */
    void handleUserAction(UserActionAvro userActionAvro);
}