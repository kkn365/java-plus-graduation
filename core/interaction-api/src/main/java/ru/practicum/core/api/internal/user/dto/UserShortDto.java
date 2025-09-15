package ru.practicum.core.api.internal.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сокращённое представление данных пользователя.
 * <p>
 * Используется для передачи минимального набора информации о пользователе.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя.
     * <p>
     * Обязательное поле. Максимальная длина — 250 символов.
     */
    private String name;
}