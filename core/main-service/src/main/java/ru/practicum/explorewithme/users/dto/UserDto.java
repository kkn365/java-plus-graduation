package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления данных пользователя.
 * <p>
 * Используется для передачи информации о пользователе между слоями приложения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
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

    /**
     * Электронная почта пользователя.
     * <p>
     * Обязательное поле. Формат: корректный email (валидация через регулярное выражение).
     * Максимальная длина — 254 символа.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Size(max = 254, message = "Максимальная длина email — 254 символа")
    @Email(message = "Некорректный формат email")
    private String email;
}