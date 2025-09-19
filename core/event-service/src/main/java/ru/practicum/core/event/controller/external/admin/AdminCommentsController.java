package ru.practicum.core.event.controller.external.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.practicum.core.event.service.api.CommentService;
import ru.practicum.core.event.dto.comments.AdminCommentParams;
import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.dto.comments.CommentPatchDto;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.util.List;

/**
 * Контроллер для управления комментариями администратором.
 * <p>
 * Обрабатывает запросы на просмотр, обновление и удаление комментариев.
 */
@Tag(name = "Admin: Комментарии", description = "API для работы с комментариями администратором")
@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentsController {

    private final CommentService commentService;

    /**
     * Получает список комментариев с фильтрацией по параметрам.
     *
     * @param params Параметры фильтрации: пользователи, события, статусы, временные рамки и пагинация
     * @return ResponseEntity со списком DTO-объектов комментариев (200 OK)
     */
    @Operation(summary = "Получить список комментариев",
            description = "Позволяет получить список комментариев с возможностью фильтрации.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "401", description = "Нет доступа"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<CommentDto>> findAllComments(AdminCommentParams params) {
        log.info("GET /admin/comments - Получен запрос на получение комментариев с параметрами: {}", params);
        List<CommentDto> comments = commentService.findAllByAdminParams(params);
        log.info("Возвращено {} комментариев", comments.size());
        return ResponseEntity.ok(comments);
    }

    /**
     * Получает комментарий по его идентификатору.
     *
     * @param commentId Идентификатор комментария
     * @return ResponseEntity с DTO-объектом комментария (200 OK)
     */
    @Operation(summary = "Получить комментарий по ID",
            description = "Позволяет получить полную информацию о комментарии по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно получен",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> findCommentById(@PathVariable long commentId) {
        log.info("GET /admin/comments/{}", commentId);
        CommentDto comment = commentService.findCommentById(commentId);
        log.info("Комментарий с ID={} успешно получен", commentId);
        return ResponseEntity.ok(comment);
    }

    /**
     * Удаляет комментарий по его идентификатору.
     *
     * @param commentId Идентификатор комментария
     * @return ResponseEntity с пустым телом и статусом 204 No Content
     */
    @Operation(summary = "Удалить комментарий",
            description = "Удаляет комментарий по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long commentId) {
        log.info("DELETE /admin/comments/{}", commentId);
        commentService.deleteComment(commentId);
        log.info("Комментарий с ID={} успешно удалён", commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновляет статус комментария.
     *
     * @param commentId         Идентификатор комментария
     * @param commentPatchDto   DTO с новым статусом
     * @return ResponseEntity с обновлённым DTO-объектом комментария (200 OK)
     */
    @Operation(summary = "Обновить статус комментария",
            description = "Обновляет статус комментария (PENDING, APPROVED, REJECTED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус комментария успешно изменён",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> patchCommentStatus(
            @PathVariable long commentId,
            @Valid @RequestBody CommentPatchDto commentPatchDto
    ) {
        log.info("PATCH /admin/comments/{} - Получен запрос на обновление статуса комментария: {}", commentId, commentPatchDto);
        CommentStatus status = commentPatchDto.getStatus();
        CommentDto updatedComment = commentService.patchCommentStatus(commentId, status);
        log.info("Статус комментария с ID={} успешно изменён на {}", commentId, status);
        return ResponseEntity.ok(updatedComment);
    }
}