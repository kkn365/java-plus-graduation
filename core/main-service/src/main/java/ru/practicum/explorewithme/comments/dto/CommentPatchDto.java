package ru.practicum.explorewithme.comments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.explorewithme.comments.model.CommentStatus;

/**
 * DTO для частичного обновления комментария.
 * <p>
 * Используется, когда требуется изменить только статус комментария.
 */
@Data
public class CommentPatchDto {

    /**
     * Новый статус комментария.
     * <p>
     * Обязательное поле. Допустимые значения: PENDING, APPROVED, REJECTED.
     */
    @NotNull(message = "Статус комментария не может быть null")
    private CommentStatus status;
}