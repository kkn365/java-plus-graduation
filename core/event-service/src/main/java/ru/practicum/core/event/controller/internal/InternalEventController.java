package ru.practicum.core.event.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.api.internal.event.client.EventClient;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.service.api.EventService;

import java.util.List;

/**
 * Контроллер для обработки внутренних запросов, связанных с событиями.
 * <p>
 * Предоставляет методы для получения информации о событиях и списка событий,
 * инициированных определённым пользователем. Реализует интерфейс {@link EventClient}.
 */
@Tag(name = "Internal: События", description = "Операции для получения информации о мероприятиях (внутренний доступ)")
@Slf4j
@RestController
@RequestMapping("/internal/event")
@RequiredArgsConstructor
public class InternalEventController implements EventClient {

    /**
     * Сервис для работы с событиями.
     */
    private final EventService eventService;

    /**
     * Получает событие по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return ResponseEntity с DTO события
     * @throws FeignException при ошибке вызова внешнего сервиса
     */
    @Operation(summary = "Получить мероприятие по ID",
            description = "Возвращает информацию о конкретном мероприятии по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о мероприятии успешно получена",
                    content = @Content(schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "404", description = "Мероприятие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Override
    public ResponseEntity<EventDto> getEventById(Long eventId) throws FeignException {
        log.info("GET /internal/event/{} - Получен внутренний запрос на получение события", eventId);
        EventDto event = eventService.findEventDtoById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Получает список событий, инициированных пользователем с указанным ID.
     *
     * @param initiatorId идентификатор пользователя-инициатора
     * @return ResponseEntity со списком DTO событий
     * @throws FeignException при ошибке вызова внешнего сервиса
     */
    @Operation(summary = "Получить список мероприятий пользователя",
            description = "Возвращает список мероприятий, инициированных пользователем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список мероприятий успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Override
    public ResponseEntity<List<EventDto>> getAllEventsByInitiatorId(Long initiatorId) throws FeignException {
        log.info("GET /internal/event?initiatorId={} - Получен внутренний запрос на получение событий", initiatorId);
        List<EventDto> events = eventService.findAllEventsByInitiatorId(initiatorId);
        return ResponseEntity.ok(events);
    }
}