package ru.practicum.explorewithme.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для создания нового комментария.
 * <p>
 * Содержит текст комментария, который должен соответствовать требованиям по длине.
 */
@Data
public class NewCommentDto {

    /**
     * Текст комментария.
     * <p>
     * Обязательное поле. Длина от 20 до 7000 символов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 20, max = 7000, message = "Текст комментария должен содержать от 20 до 7000 символов")
    private String text;
}