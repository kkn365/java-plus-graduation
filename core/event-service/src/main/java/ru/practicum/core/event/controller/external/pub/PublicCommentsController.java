package ru.practicum.core.event.controller.external.pub;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.service.api.CommentService;

/**
 * Контроллер для работы с комментариями к событиям, доступными публично.
 * <p>
 * Обрабатывает GET-запросы на получение списка комментариев и отдельного комментария.
 */
@Tag(name = "Public: Комментарии",
        description = "Операции для получения информации о комментариях к событиям (публичный доступ)")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/events")
public class PublicCommentsController {

    private final CommentService commentService;

    /**
     * Возвращает список комментариев к указанному событию.
     *
     * @param eventId Идентификатор события
     * @return ResponseEntity со списком DTO комментариев
     */
    @Operation(summary = "Получить список комментариев к событию",
            description = "Возвращает список всех комментариев, связанных с событием.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "404", description = "Событие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long eventId) {
        log.info("GET /events/{}/comments", eventId);
        List<CommentDto> comments = commentService.findComments(eventId);
        log.info("Возвращено {} комментариев к событию с ID={}", comments.size(), eventId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Возвращает конкретный комментарий по его идентификатору и идентификатору события.
     *
     * @param eventId    Идентификатор события
     * @param commentId  Идентификатор комментария
     * @return ResponseEntity с DTO комментария
     */
    @Operation(summary = "Получить комментарий по ID",
            description = "Возвращает информацию о конкретном комментарии к событию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о комментарии успешно получена",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Событие или комментарий не найдены"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{eventId}/comments/{commentId}")
    public ResponseEntity<CommentDto> getComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId
    ) {
        log.info("GET /events/{}/comments/{}", eventId, commentId);
        CommentDto commentDto = commentService.findComment(eventId, commentId);
        log.info("Возвращен комментарий с ID={} к событию с ID={}", commentId, eventId);
        return ResponseEntity.ok(commentDto);
    }
}