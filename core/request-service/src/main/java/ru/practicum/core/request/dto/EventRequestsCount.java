package ru.practicum.core.request.dto;

/**
 * DTO-объект, представляющий количество подтверждённых запросов на участие в событии.
 * <p>
 * Используется для передачи статистики по заявкам на события между слоями приложения.
 */
public record EventRequestsCount(
        Long eventId,
        Long confirmedRequests
) {}