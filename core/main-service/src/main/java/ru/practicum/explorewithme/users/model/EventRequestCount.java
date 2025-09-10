package ru.practicum.explorewithme.users.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Класс для хранения агрегированных данных о количестве заявок на событие.
 * <p>
 * Используется в запросах, где необходимо получить количество заявок по событиям.
 */
@Getter
@AllArgsConstructor
@ToString
public class EventRequestCount {
    /**
     * Идентификатор события.
     */
    private final Long eventId;

    /**
     * Количество заявок на событие.
     */
    private final Long requestsCount;
}