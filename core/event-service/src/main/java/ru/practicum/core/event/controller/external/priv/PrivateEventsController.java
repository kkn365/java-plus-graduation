package ru.practicum.core.event.controller.external.priv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.service.api.EventService;

import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.*;

/**
 * Контроллер для работы с событиями, доступными только авторизованному пользователю.
 * <p>
 * Обрабатывает запросы на создание, обновление, получение информации о событиях и список событий пользователя.
 */
@Tag(name = "Private: События", description = "Операции для управления событиями пользователя (авторизованный пользователь)")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateEventsController {

    private final EventService eventService;

    /**
     * Возвращает список событий, принадлежащих указанному пользователю, с пагинацией.
     *
     * @param userId Идентификатор пользователя
     * @param from   Смещение для пагинации (по умолчанию 0)
     * @param size   Количество элементов на странице (по умолчанию 10)
     * @return ResponseEntity со списком DTO событий
     */
    @Operation(summary = "Получить список событий пользователя",
            description = "Возвращает список событий, принадлежащих авторизованному пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список событий успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{userId}/events")
    public ResponseEntity<List<EventDto>> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /users/{}/events?from={}&size={}", userId, from, size);
        List<EventDto> events = eventService.findAllByParams(userId, from, size);
        log.info("Возвращено {} событий пользователя с ID {}", events.size(), userId);
        return ResponseEntity.ok(events);
    }

    /**
     * Создаёт новое событие от имени указанного пользователя.
     *
     * @param userId   Идентификатор пользователя
     * @param eventDto DTO с данными события
     * @return ResponseEntity с DTO созданного события и статусом CREATED
     */
    @Operation(summary = "Создать событие",
            description = "Позволяет авторизованному пользователю создать новое событие.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Событие успешно создано",
                    content = @Content(schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PostMapping("/{userId}/events")
    public ResponseEntity<EventDto> createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto eventDto
    ) {
        log.info("POST /users/{}/events", userId);
        EventDto createdEvent = eventService.addEvent(eventDto, userId);
        log.info("Событие с ID={} успешно создано для пользователя с ID={}", createdEvent.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Возвращает информацию о конкретном событии, принадлежащем пользователю.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return ResponseEntity с DTO события
     */
    @Operation(summary = "Получить событие по ID",
            description = "Возвращает информацию о событии, принадлежащем авторизованному пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о событии успешно получена",
                    content = @Content(schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "404", description = "Событие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> getUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        EventDto event = eventService.findUserEvent(userId, eventId);
        log.info("Событие с ID={} успешно получено для пользователя с ID={}", event.getId(), userId);
        return ResponseEntity.ok(event);
    }

    /**
     * Обновляет данные события, принадлежащего пользователю.
     *
     * @param userId    Идентификатор пользователя
     * @param eventId   Идентификатор события
     * @param eventDto  DTO с обновлёнными данными события
     * @return ResponseEntity с DTO обновлённого события
     */
    @Operation(summary = "Обновить событие",
            description = "Позволяет авторизованному пользователю обновить данные своего события.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Событие успешно обновлено",
                    content = @Content(schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Событие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PatchMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest eventDto
    ) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        EventDto updatedEvent = eventService.updateEventByUser(eventId, eventDto, userId);
        log.info("Событие с ID={} успешно обновлено для пользователя с ID={}", updatedEvent.getId(), userId);
        return ResponseEntity.ok(updatedEvent);
    }
}