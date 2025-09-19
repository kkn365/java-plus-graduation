package ru.practicum.core.event.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.DateTimeFormatConstants.DATE_TIME_FORMAT;

/**
 * DTO для фильтрации комментариев администратором.
 * <p>
 * Содержит параметры, используемые в спецификации запроса к базе данных.
 */
@Getter
@Setter
@Builder
public class AdminCommentParams {

    /**
     * Список идентификаторов комментариев для фильтрации.
     * Может быть пустым.
     */
    @Schema(description = "Список идентификаторов комментариев", example = "[1, 2, 3]")
    private List<Long> comments;

    /**
     * Текст для поиска (поиск по содержимому комментария).
     * Может быть null.
     */
    @Schema(
            description = "Текст для поиска в комментарии",
            example = "Очень полезное событие для саморазвития!")
    private String text;

    /**
     * Список идентификаторов событий, к которым относятся комментарии.
     * Может быть пустым.
     */
    @Schema(description = "Список идентификаторов событий", example = "[100, 101]")
    private List<Long> events;

    /**
     * Список идентификаторов авторов комментариев.
     * Может быть пустым.
     */
    @Schema(description = "Список идентификаторов авторов", example = "[10, 11]")
    private List<Long> authors;

    /**
     * Список статусов комментариев.
     * Может быть пустым.
     */
    @Schema(
            description = "Список статусов комментариев",
            example = "[PENDING, PUBLISHED]",
            allowableValues = {"PENDING", "PUBLISHED", "REJECTED"})
    private List<CommentStatus> status;

    /**
     * Начальная дата создания комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Начальная дата создания комментария", example = "2025-04-01 10:00:00")
    private LocalDateTime createdDateStart;

    /**
     * Конечная дата создания комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Конечная дата создания комментария", example = "2025-04-07 23:59:59")
    private LocalDateTime createdDateEnd;

    /**
     * Начальная дата публикации комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Начальная дата публикации комментария", example = "2025-04-02 12:00:00")
    private LocalDateTime publishedDateStart;

    /**
     * Конечная дата публикации комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Конечная дата публикации комментария", example = "2025-04-08 23:59:59")
    private LocalDateTime publishedDateEnd;

    /**
     * Смещение для пагинации. По умолчанию 0.
     */
    @Min(0)
    @Schema(description = "Смещение для пагинации", example = "0", minimum = "0")
    private Integer from;

    /**
     * Размер страницы. По умолчанию 10.
     */
    @Min(1)
    @Schema(description = "Размер страницы", example = "10", minimum = "1")
    private Integer size;
}