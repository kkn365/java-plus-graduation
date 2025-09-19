package ru.practicum.core.event.controller.external.priv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.dto.comments.NewCommentDto;
import ru.practicum.core.event.service.api.CommentService;

import java.util.List;

/**
 * Контроллер для работы с комментариями, доступными только авторизованному пользователю.
 * <p>
 * Обрабатывает запросы на создание комментариев и получение утверждённых комментариев пользователя.
 */
@Tag(
        name = "Private::Комментарии",
        description = "API для работы с комментариями, доступными только авторизованному пользователю")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateCommentsController {

    private final CommentService commentService;

    /**
     * Создаёт новый комментарий к событию от имени указанного пользователя.
     *
     * @param userId       Идентификатор пользователя
     * @param eventId      Идентификатор события
     * @param newCommentDto DTO с данными нового комментария
     * @return ResponseEntity с DTO созданного комментария и статусом CREATED
     */
    @Operation(summary = "Создать комментарий к событию",
            description = "Позволяет авторизованному пользователю оставить комментарий к событию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Комментарий успешно создан",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Событие или пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PostMapping("/{userId}/events/{eventId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto
    ) {
        log.info("POST /users/{}/events/{}/comments", userId, eventId);
        CommentDto createdComment = commentService.createComment(userId, eventId, newCommentDto);
        log.info("Комментарий с ID={} успешно создан для пользователя с ID={}", createdComment.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * Возвращает список утверждённых комментариев, оставленных пользователем.
     *
     * @param userId Идентификатор пользователя
     * @return ResponseEntity со списком DTO комментариев
     */
    @Operation(summary = "Получить утверждённые комментарии пользователя",
            description = "Возвращает список комментариев, оставленных пользователем и одобренных модератором.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{userId}/comments")
    public ResponseEntity<List<CommentDto>> findApprovedCommentsOnUser(@PathVariable Long userId) {
        log.info("GET /users/{}/comments", userId);
        List<CommentDto> comments = commentService.findApprovedCommentsOnUserId(userId);
        log.info("Найдено {} утверждённых комментариев пользователя с ID={}", comments.size(), userId);
        return ResponseEntity.ok(comments);
    }
}