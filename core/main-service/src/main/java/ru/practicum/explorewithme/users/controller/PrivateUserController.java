package ru.practicum.explorewithme.users.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comments.service.CommentService;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.events.service.EventService;
import ru.practicum.explorewithme.users.dto.ChangeRequestStatusDto;
import ru.practicum.explorewithme.users.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.users.dto.UserParticipationRequestDto;
import ru.practicum.explorewithme.users.service.RequestService;

import java.util.List;

import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateUserController {

    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    /**
     * Получение всех заявок пользователя.
     * <p>
     * Логирует входящий запрос и возвращает список заявок.
     *
     * @param userId Идентификатор пользователя
     * @return HTTP-ответ со списком заявок и статусом OK
     */
    @GetMapping("/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> findAllRequests(@PathVariable long userId) {
        log.info("Получен GET-запрос на получение всех заявок пользователя {}", userId);
        List<ParticipationRequestDto> requests = requestService.getAllRequestsByUser(userId);
        log.info("Найдено {} заявок для пользователя {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Создание новой заявки на участие в событии.
     * <p>
     * Логирует входящие параметры и возвращает созданную заявку.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return HTTP-ответ с созданной заявкой и статусом CREATED
     */
    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> save(
            @PathVariable long userId,
            @RequestParam("eventId") @NotNull long eventId) {
        log.info("Получен POST-запрос на создание заявки на участие в событии {} для пользователя {}", eventId, userId);
        ParticipationRequestDto createdRequest = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    /**
     * Отмена заявки пользователя.
     * <p>
     * Логирует идентификаторы пользователя и заявки.
     *
     * @param userId     Идентификатор пользователя
     * @param requestId  Идентификатор заявки
     * @return HTTP-ответ с обновлённой заявкой и статусом OK
     */
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable long userId,
            @PathVariable long requestId) {
        log.info("Получен PATCH-запрос на отмену заявки {} пользователя {}", requestId, userId);
        ParticipationRequestDto updatedRequest = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Получение событий пользователя с пагинацией.
     * <p>
     * Логирует параметры пагинации и возвращает список событий.
     *
     * @param userId Идентификатор пользователя
     * @param from   Начальная позиция (смещение)
     * @param to     Количество элементов на странице
     * @return HTTP-ответ со списком событий и статусом OK
     */
    @GetMapping("/{userId}/events")
    public ResponseEntity<List<EventDto>> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) int to) {
        log.info("Получен GET-запрос на получение событий пользователя {} с параметрами: from={}, to={}",
                userId, from, to);
        List<EventDto> events = eventService.findAllByParams(userId, from, to);
        log.info("Найдено {} событий для пользователя {}", events.size(), userId);
        return ResponseEntity.ok(events);
    }

    /**
     * Создание нового события пользователем.
     * <p>
     * Логирует данные события и возвращает созданное событие.
     *
     * @param userId    Идентификатор пользователя
     * @param eventDto  Данные события
     * @return HTTP-ответ с событием и статусом CREATED
     */
    @PostMapping("/{userId}/events")
    public ResponseEntity<EventDto> createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto eventDto) {
        log.info("Получен POST-запрос на создание события {} пользователем {}", eventDto.getTitle(), userId);
        EventDto createdEvent = eventService.addEvent(eventDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Получение информации о событии пользователя.
     * <p>
     * Логирует идентификаторы пользователя и события.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return HTTP-ответ с событием и статусом OK
     */
    @GetMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> getUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Получен GET-запрос на получение события {} пользователя {}", eventId, userId);
        EventDto event = eventService.findUserEvent(userId, eventId);
        log.info("Событие {} найдено", eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Обновление события пользователем.
     * <p>
     * Логирует изменения и возвращает обновлённое событие.
     *
     * @param userId    Идентификатор пользователя
     * @param eventId   Идентификатор события
     * @param eventDto  Данные для обновления
     * @return HTTP-ответ с событием и статусом OK
     */
    @PatchMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest eventDto) {
        log.info("Получен PATCH-запрос на обновление события {} пользователем {}", eventId, userId);
        EventDto updatedEvent = eventService.updateEventByUser(eventId, eventDto, userId);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Получение заявок на событие пользователя.
     * <p>
     * Логирует идентификаторы пользователя и события.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return HTTP-ответ со списком заявок и статусом OK
     */
    @GetMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> findUserRequestsOnEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Получен GET-запрос на получение заявок на событие {} пользователя {}", eventId, userId);
        List<ParticipationRequestDto> requests = requestService.getUserRequestsForEvent(userId, eventId);
        log.info("Найдено {} заявок на событие {}", requests.size(), eventId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Изменение статуса заявок на событие.
     * <p>
     * Логирует параметры запроса и возвращает результат.
     *
     * @param changeRequestStatusDto Данные для изменения статуса
     * @param userId                 Идентификатор пользователя
     * @param eventId                Идентификатор события
     * @return HTTP-ответ с результатом и статусом OK
     */
    @PatchMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<UserParticipationRequestDto> patchRequestStatus(
            @Valid @RequestBody ChangeRequestStatusDto changeRequestStatusDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Получен PATCH-запрос на изменение статуса заявок на событие {} пользователя {}", eventId, userId);
        UserParticipationRequestDto result = requestService.updateRequestStatus(changeRequestStatusDto, userId, eventId);
        log.info("Обработано {} подтверждённых и {} отклонённых заявок",
                result.getConfirmedRequests().size(),
                result.getRejectedRequests().size());
        return ResponseEntity.ok(result);
    }

    /**
     * Создание комментария к событию.
     * <p>
     * Логирует текст комментария и возвращает созданную сущность.
     *
     * @param userId       Идентификатор пользователя
     * @param eventId      Идентификатор события
     * @param newCommentDto Данные комментария
     * @return HTTP-ответ с комментарием и статусом CREATED
     */
    @PostMapping("/{userId}/events/{eventId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Получен POST-запрос на создание комментария {} к событию {} пользователем {}",
                newCommentDto.getText(), eventId, userId);
        CommentDto createdComment = commentService.createComment(userId, eventId, newCommentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * Получение утверждённых комментариев пользователя.
     * <p>
     * Логирует идентификатор пользователя и возвращает список.
     *
     * @param userId Идентификатор пользователя
     * @return HTTP-ответ со списком комментариев и статусом OK
     */
    @GetMapping("/{userId}/comments")
    public ResponseEntity<List<CommentDto>> findApprovedCommentsOnUser(@PathVariable Long userId) {
        log.info("Получен GET-запрос на получение утверждённых комментариев пользователя {}", userId);
        List<CommentDto> comments = commentService.findApprovedCommentsOnUserId(userId);
        log.info("Найдено {} утверждённых комментариев", comments.size());
        return ResponseEntity.ok(comments);
    }
}