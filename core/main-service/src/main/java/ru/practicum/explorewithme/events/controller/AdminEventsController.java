package ru.practicum.explorewithme.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.util.DateTimeFormatConstants.DATE_TIME_FORMAT;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

import ru.practicum.explorewithme.events.dto.AdminEventParams;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.events.service.EventService;

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
            @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size
    ) {
        log.info("Получен GET-запрос на получение событий с параметрами: users={}, states={}, categories={}, " +
                 "rangeStart={}, rangeEnd={}, from={}, size={}",
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
        log.info("Получен PATCH-запрос на обновление события с ID {} и данными: {}", eventId, eventDto);

        EventDto updatedEvent = eventService.updateEventByAdmin(eventId, eventDto);
        log.info("Событие с ID {} успешно обновлено", eventId);

        return ResponseEntity.ok().body(updatedEvent);
    }
}