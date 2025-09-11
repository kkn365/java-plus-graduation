package ru.practicum.explorewithme.exception.model;

import org.springframework.http.HttpStatus;

/**
 * Модель ошибки для ответов API.
 * <p>
 * Содержит статус HTTP-запроса, краткое описание ошибки и подробное сообщение.
 */
public record ApiError(
        HttpStatus status,
        String title,
        String message
) {
}