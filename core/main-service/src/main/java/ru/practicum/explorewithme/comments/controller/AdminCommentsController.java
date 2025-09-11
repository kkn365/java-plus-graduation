package ru.practicum.explorewithme.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.comments.service.CommentService;
import ru.practicum.explorewithme.comments.dto.AdminCommentParams;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.CommentPatchDto;
import ru.practicum.explorewithme.comments.model.CommentStatus;

import java.util.List;

/**
 * Контроллер для управления комментариями администратором.
 * <p>
 * Обрабатывает запросы на просмотр, обновление и удаление комментариев.
 */
@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentsController {

    private final CommentService commentService;

    /**
     * Получает список комментариев с фильтрацией по параметрам.
     *
     * @param params параметры фильтрации
     * @return список DTO-объектов комментариев (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<CommentDto>> findAllComments(AdminCommentParams params) {
        log.info("GET /admin/comments - Получен запрос на получение списка комментариев с параметрами: {}", params);
        List<CommentDto> comments = commentService.findAllByAdminParams(params);
        return ResponseEntity.ok(comments);
    }

    /**
     * Получает комментарий по его идентификатору.
     *
     * @param commentId идентификатор комментария
     * @return DTO-объект комментария (200 OK)
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> findCommentById(@PathVariable long commentId) {
        log.info("GET /admin/comments/{} - Получен запрос на получение комментария", commentId);
        CommentDto comment = commentService.findCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    /**
     * Удаляет комментарий по его идентификатору.
     *
     * @param commentId идентификатор комментария
     * @return пустой ответ (204 No Content)
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long commentId) {
        log.info("DELETE /admin/comments/{} - Получен запрос на удаление комментария", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновляет статус комментария.
     *
     * @param commentId             идентификатор комментария
     * @param commentPatchDto       DTO с новым статусом
     * @return обновлённый DTO-объект комментария (200 OK)
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> patchCommentStatus(
            @PathVariable long commentId,
            @Valid @RequestBody CommentPatchDto commentPatchDto) {
        log.info("PATCH /admin/comments/{} - Получен запрос на обновление статуса комментария: {}", commentId, commentPatchDto);
        CommentStatus status = commentPatchDto.getStatus();
        CommentDto updatedComment = commentService.patchCommentStatus(commentId, status);
        return ResponseEntity.ok(updatedComment);
    }
}