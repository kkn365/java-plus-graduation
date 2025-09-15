package ru.practicum.core.event.dto.comments;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

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