package ru.practicum.explorewithme.comments.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.comments.model.CommentStatus;

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
    private Long id;

    /**
     * Текст комментария.
     */
    private String text;

    /**
     * Идентификатор события, к которому относится комментарий.
     */
    private Long eventId;

    /**
     * Идентификатор автора комментария.
     */
    private Long authorId;

    /**
     * Дата и время создания комментария.
     */
    private LocalDateTime createdDate;

    /**
     * Дата и время последнего обновления комментария.
     */
    private LocalDateTime updatedDate;

    /**
     * Дата и время публикации комментария.
     * <p>
     * Заполняется только если статус = APPROVED.
     */
    private LocalDateTime publishedDate;

    /**
     * Текущий статус комментария.
     * <p>
     * Возможные значения: PENDING, APPROVED, REJECTED.
     */
    private CommentStatus status;
}