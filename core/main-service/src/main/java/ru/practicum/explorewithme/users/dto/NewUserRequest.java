package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового пользователя.
 * <p>
 * Содержит обязательные поля: имя и электронная почта.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    /**
     * Имя пользователя.
     * <p>
     * Обязательное поле. Длина от 2 до 250 символов.
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250 символов")
    private String name;

    /**
     * Электронная почта пользователя.
     * <p>
     * Обязательное поле. Длина от 6 до 254 символов. Формат: корректный email.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(min = 6, max = 254, message = "Длина email должна быть от 6 до 254 символов")
    private String email;
}