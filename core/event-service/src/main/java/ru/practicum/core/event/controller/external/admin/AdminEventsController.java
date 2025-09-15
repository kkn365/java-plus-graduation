package ru.practicum.core.event.controller.external.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.DateTimeFormatConstants.DATE_TIME_FORMAT;
import static ru.practicum.core.api.util.constants.PaginationConstants.*;

import ru.practicum.core.event.dto.events.AdminEventParams;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.event.service.api.EventService;

/**
 * Контроллер для работы с событиями в админской части.
 * <p>
 * Обрабатывает запросы на получение и обновление событий, фильтруя по параметрам: пользователь, категория,
 * статус события, временной диапазон и пагинация.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/events")
public class AdminEventsController {

    private final EventService eventService;

    /**
     * Получает список событий по фильтру.
     * <p>
     * Поддерживает фильтрацию по пользователям, категориям, статусу событий и временному диапазону.
     *
     * @param users      Список идентификаторов пользователей
     * @param states     Список статусов событий
     * @param categories Список идентификаторов категорий
     * @param rangeStart Начало временного диапазона
     * @param rangeEnd   Конец временного диапазона
     * @param from       Начальная позиция для пагинации
     * @param size       Размер страницы для пагинации
     * @return ResponseEntity со списком DTO событий
     */
    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /admin/events?users={}&states={}&categories={}&rangeStart={}&rangeEnd={}&from={}&size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        AdminEventParams adminEventParams = AdminEventParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        List<EventDto> events = eventService.findAllByAdminParams(adminEventParams);
        log.info("Возвращено {} событий", events.size());

        return ResponseEntity.ok().body(events);
    }

    /**
     * Обновляет событие администратором.
     * <p>
     * Позволяет изменить состояние события или другие его параметры.
     *
     * @param eventId  Идентификатор события
     * @param eventDto DTO с данными для обновления события
     * @return ResponseEntity с обновлённым DTO события
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest eventDto
    ) {
        log.info("PATCH /admin/events/{}", eventId);

        EventDto updatedEvent = eventService.updateEventByAdmin(eventId, eventDto);
        log.info("Событие с ID={} успешно обновлено", eventId);

        return ResponseEntity.ok().body(updatedEvent);
    }
}