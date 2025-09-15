package ru.practicum.core.event.controller.external.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.service.api.CommentService;

import java.util.List;

/**
 * Контроллер для работы с комментариями к событиям, доступными публично.
 * <p>
 * Обрабатывает GET-запросы на получение списка комментариев и отдельного комментария.
 */
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