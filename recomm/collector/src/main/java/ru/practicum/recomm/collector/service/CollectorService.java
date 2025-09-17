package ru.practicum.recomm.collector.service;

import ru.practicum.recommendations.messages.UserActionProto;

/**
 * Интерфейс CollectorService определяет методы для обработки пользовательских действий.
 * <p>
 * Используется для приема данных о взаимодействии пользователей с мероприятиями в формате UserActionProto.
 */
public interface CollectorService {

    /**
     * Метод для обработки нового действия пользователя.
     * <p>
     * Принимает объект {@link UserActionProto}, содержащий информацию о пользователе, мероприятии,
     * времени взаимодействия и весе (уровне интереса). Данные могут использоваться
     * для дальнейшего анализа, хранения или генерации рекомендаций.
     *
     * @param actionProto объект, содержащий данные о действии пользователя
     */
    void newUserAction(UserActionProto actionProto);
}