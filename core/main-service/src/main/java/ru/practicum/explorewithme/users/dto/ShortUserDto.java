package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ShortUserDto {
    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя.
     * <p>
     * Обязательное поле. Максимальная длина — 250 символов.
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 250, message = "Максимальная длина имени — 250 символов")
    private String name;
}