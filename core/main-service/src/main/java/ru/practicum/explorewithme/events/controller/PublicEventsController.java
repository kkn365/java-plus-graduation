package ru.practicum.explorewithme.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comments.service.CommentService;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.UserEventParams;
import ru.practicum.explorewithme.events.enumeration.EventSortEnum;
import ru.practicum.explorewithme.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.util.DateTimeFormatConstants.DATE_TIME_FORMAT;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

/**
 * Контроллер для работы с событиями в публичной части.
 * <p>
 * Обрабатывает запросы на получение событий, отдельного события и его комментариев.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/events")
public class PublicEventsController {

    private final EventService eventService;
    private final CommentService commentService;

    /**
     * Получает список событий по фильтру.
     * <p>
     * Поддерживает фильтрацию по тексту, категориям, платности, доступности,
     * временному диапазону и пагинации. Результат может быть отсортирован.
     *
     * @param request        Объект HTTP-запроса
     * @param text           Текст для поиска в заголовке и описании события
     * @param categories     Список идентификаторов категорий
     * @param paid           Признак платности события
     * @param onlyAvailable  Признак наличия свободных мест
     * @param rangeStart     Начало временного диапазона
     * @param rangeEnd       Конец временного диапазона
     * @param sort           Критерий сортировки (дата или просмотры)
     * @param from           Начальная позиция для пагинации
     * @param size           Размер страницы для пагинации
     * @return ResponseEntity со списком DTO событий
     */
    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(
            HttpServletRequest request,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(required = false) EventSortEnum sort,
            @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size
    ) {
        log.info("Получен GET-запрос на получение событий с параметрами: text={}, categories={}, paid={}, onlyAvailable={}, rangeStart={}, rangeEnd={}, sort={}, from={}, size={}",
                text, categories, paid, onlyAvailable, rangeStart, rangeEnd, sort, from, size);

        UserEventParams userEventParams = UserEventParams
                .builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        eventService.sendHit(request);
        List<EventDto> events = eventService.findAllByUserParams(userEventParams);
        log.info("Возвращено {} событий", events.size());

        return ResponseEntity.ok().body(events);
    }

    /**
     * Получает событие по его ID.
     * <p>
     * Возвращает событие только если оно находится в состоянии ПУБЛИКОВАНО.
     *
     * @param request   Объект HTTP-запроса
     * @param eventId   Идентификатор события
     * @return ResponseEntity с DTO события
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getPublishedEvent(
            HttpServletRequest request,
            @NotNull @PathVariable Long eventId
    ) {
        log.info("Получен GET-запрос на получение события с ID {}", eventId);

        eventService.sendHit(request);
        EventDto eventDto = eventService.findPublishedEvent(eventId);
        log.info("Событие с ID {} успешно получено", eventId);

        return ResponseEntity.ok().body(eventDto);
    }

    /**
     * Получает список комментариев к событию.
     *
     * @param eventId Идентификатор события
     * @return ResponseEntity со списком DTO комментариев
     */
    @GetMapping("/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long eventId) {
        log.info("Получен GET-запрос на получение комментариев к событию с ID {}", eventId);

        List<CommentDto> comments = commentService.findComments(eventId);
        log.info("Возвращено {} комментариев к событию с ID {}", comments.size(), eventId);

        return ResponseEntity.ok().body(comments);
    }

    /**
     * Получает конкретный комментарий к событию по его ID.
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
        log.info("Получен GET-запрос на получение комментария с ID {} к событию с ID {}", commentId, eventId);

        CommentDto commentDto = commentService.findComment(eventId, commentId);
        log.info("Комментарий с ID {} к событию с ID {} успешно получен", commentId, eventId);

        return ResponseEntity.ok().body(commentDto);
    }
}