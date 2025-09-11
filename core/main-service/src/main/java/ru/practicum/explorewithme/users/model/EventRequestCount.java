package ru.practicum.explorewithme.users.model;

/**
 * Модель для хранения количества запросов на участие в событии.
 * <p>
 * Используется для агрегирования данных о запросах на участие в событиях.
 */
public record EventRequestCount(
        Long eventId,
        Long requestsCount
) {
}