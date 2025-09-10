package ru.practicum.explorewithme.events.enumeration;

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
     * Доступно только для событий в состоянии {@link ru.practicum.explorewithme.events.enumeration.EventState#PENDING}.
     */
    PUBLISH_EVENT,

    /**
     * Отклонить событие.
     * <p>
     * Доступно для событий в состоянии {@link ru.practicum.explorewithme.events.enumeration.EventState#PENDING}.
     */
    REJECT_EVENT,

    /**
     * Отправить событие на повторную проверку.
     * <p>
     * Доступно для событий в состоянии {@link ru.practicum.explorewithme.events.enumeration.EventState#CANCELED}.
     */
    SEND_TO_REVIEW,

    /**
     * Отменить отправку события на проверку.
     * <p>
     * Доступно для событий в состоянии {@link ru.practicum.explorewithme.events.enumeration.EventState#PENDING}.
     */
    CANCEL_REVIEW
}