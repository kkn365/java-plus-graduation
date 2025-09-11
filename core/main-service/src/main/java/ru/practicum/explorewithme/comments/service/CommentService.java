package ru.practicum.explorewithme.comments.service;

import ru.practicum.explorewithme.comments.dto.AdminCommentParams;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.comments.model.CommentStatus;

import java.util.List;

/**
 * Интерфейс сервиса для управления комментариями.
 * <p>
 * Предоставляет методы для поиска, создания, удаления и обновления комментариев.
 */
public interface CommentService {

    /**
     * Получает список всех комментариев к событию.
     *
     * @param eventId идентификатор события
     * @return список DTO-объектов комментариев
     */
    List<CommentDto> findComments(long eventId);

    /**
     * Получает конкретный комментарий по его идентификатору и идентификатору события.
     *
     * @param eventId      идентификатор события
     * @param commentId    идентификатор комментария
     * @return DTO-объект комментария
     */
    CommentDto findComment(long eventId, long commentId);

    /**
     * Получает комментарий по его идентификатору.
     *
     * @param commentId    идентификатор комментария
     * @return DTO-объект комментария
     */
    CommentDto findCommentById(long commentId);

    /**
     * Создаёт новый комментарий к событию от имени пользователя.
     *
     * @param userId         идентификатор автора
     * @param eventId        идентификатор события
     * @param newCommentDto  DTO с текстом комментария
     * @return DTO-объект созданного комментария
     */
    CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto);

    /**
     * Удаляет комментарий по его идентификатору.
     *
     * @param commentId    идентификатор комментария
     */
    void deleteComment(long commentId);

    /**
     * Обновляет статус комментария.
     *
     * @param commentId    идентификатор комментария
     * @param status       новый статус комментария
     * @return DTO-объект обновлённого комментария
     */
    CommentDto patchCommentStatus(long commentId, CommentStatus status);

    /**
     * Получает список опубликованных комментариев конкретного пользователя.
     *
     * @param userId    идентификатор пользователя
     * @return список DTO-объектов комментариев
     */
    List<CommentDto> findApprovedCommentsOnUserId(long userId);

    /**
     * Получает список комментариев по параметрам фильтрации (для администратора).
     *
     * @param params    параметры фильтрации
     * @return список DTO-объектов комментариев
     */
    List<CommentDto> findAllByAdminParams(AdminCommentParams params);
}