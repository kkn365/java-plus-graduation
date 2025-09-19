package ru.practicum.core.event.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.time.LocalDateTime;

/**
 * DTO для представления комментария.
 * <p>
 * Используется при возврате данных клиенту. Содержит информацию о тексте, событии,
 * авторе, статусе и временных метках.
 */
@Data
@Builder
public class CommentDto {

    /**
     * Уникальный идентификатор комментария.
     */
    @Schema(description = "Уникальный идентификатор комментария", example = "123")
    private Long id;

    /**
     * Текст комментария.
     */
    @Schema(description = "Текст комментария", example = "Отличное мероприятие!")
    private String text;

    /**
     * Идентификатор события, к которому относится комментарий.
     */
    @Schema(description = "Идентификатор события", example = "1001")
    private Long eventId;

    /**
     * Идентификатор автора комментария.
     */
    @Schema(description = "Идентификатор автора комментария", example = "456")
    private Long authorId;

    /**
     * Дата и время создания комментария.
     */
    @Schema(description = "Дата и время создания комментария", example = "2025-04-05T14:30:00")
    private LocalDateTime createdDate;

    /**
     * Дата и время последнего обновления комментария.
     */
    @Schema(description = "Дата и время последнего обновления комментария", example = "2025-04-05T15:00:00")
    private LocalDateTime updatedDate;

    /**
     * Дата и время публикации комментария.
     * <p>
     * Заполняется только если статус = APPROVED.
     */
    @Schema(description = "Дата и время публикации комментария", example = "2025-04-06T08:00:00")
    private LocalDateTime publishedDate;

    /**
     * Текущий статус комментария.
     * <p>
     * Возможные значения: PENDING, APPROVED, REJECTED.
     */
    @Schema(
            description = "Статус комментария",
            example = "APPROVED",
            allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private CommentStatus status;
}