package ru.practicum.explorewithme.comments.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.explorewithme.comments.model.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.util.DateTimeFormatConstants.DATE_TIME_FORMAT;

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
    private List<Long> comments;

    /**
     * Текст для поиска (поиск по содержимому комментария).
     * Может быть null.
     */
    private String text;

    /**
     * Список идентификаторов событий, к которым относятся комментарии.
     * Может быть пустым.
     */
    private List<Long> events;

    /**
     * Список идентификаторов авторов комментариев.
     * Может быть пустым.
     */
    private List<Long> authors;

    /**
     * Список статусов комментариев.
     * Может быть пустым.
     */
    private List<CommentStatus> status;

    /**
     * Начальная дата создания комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdDateStart;

    /**
     * Конечная дата создания комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdDateEnd;

    /**
     * Начальная дата публикации комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime publishedDateStart;

    /**
     * Конечная дата публикации комментариев.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime publishedDateEnd;

    /**
     * Смещение для пагинации. По умолчанию 0.
     */
    private Integer from;

    /**
     * Размер страницы. По умолчанию 10.
     */
    private Integer size;
}