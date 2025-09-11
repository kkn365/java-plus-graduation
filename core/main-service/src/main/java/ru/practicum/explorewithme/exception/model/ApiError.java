package ru.practicum.explorewithme.exception.model;

import org.springframework.http.HttpStatus;

/**
 * Модель ошибки для ответов API.
 * <p>
 * Содержит статус HTTP-запроса, краткое описание ошибки и подробное сообщение.
 */
public record ApiError(
        HttpStatus status,     // HTTP-статус ошибки
        String title,          // Краткое описание типа ошибки (например, "Ошибка валидации")
        String message         // Подробное сообщение об ошибке
) {
}