package ru.practicum.core.event.model.enums.events;

import ru.practicum.core.api.util.enums.EventState;

/**
 * Перечисление, представляющее возможные действия над состоянием события.
 * <p>
 * Используется для управления переходами между состояниями событий и ограничения допустимых операций в зависимости
 * от текущего состояния события.
 */
public enum EventStateAction {
    /**
     * Опубликовать событие.
     * <p>
     * Доступно только для событий в состоянии {@link EventState#PENDING}.
     */
    PUBLISH_EVENT,

    /**
     * Отклонить событие.
     * <p>
     * Доступно для событий в состоянии {@link EventState#PENDING}.
     */
    REJECT_EVENT,

    /**
     * Отправить событие на повторную проверку.
     * <p>
     * Доступно для событий в состоянии {@link EventState#CANCELED}.
     */
    SEND_TO_REVIEW,

    /**
     * Отменить отправку события на проверку.
     * <p>
     * Доступно для событий в состоянии {@link EventState#PENDING}.
     */
    CANCEL_REVIEW
}