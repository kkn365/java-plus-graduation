package ru.practicum.core.event.controller.external.pub;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.event.model.enums.events.EventSort;
import ru.practicum.core.event.service.api.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.DateTimeFormatConstants.DATE_TIME_FORMAT;
import static ru.practicum.core.api.util.constants.PaginationConstants.*;

/**
 * Контроллер для публичного доступа к событиям.
 * <p>
 * Обрабатывает GET-запросы на получение списка событий и отдельного события.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventsController {

    private final EventService eventService;

    /**
     * Возвращает список событий, соответствующих заданным фильтрам.
     *
     * @param request        HTTP-запрос (используется для сбора статистики)
     * @param text           Текст для поиска в заголовке и описании
     * @param categories     Список идентификаторов категорий
     * @param paid           Признак платности события
     * @param onlyAvailable  Признак наличия свободных мест
     * @param rangeStart     Начало временного диапазона
     * @param rangeEnd       Конец временного диапазона
     * @param sort           Критерий сортировки
     * @param from           Смещение для пагинации
     * @param size           Размер страницы
     * @return ResponseEntity со списком событий
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
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /events?text={}&categories={}&paid={}&onlyAvailable={}&rangeStart={}&rangeEnd={}&sort={}&from={}&size={}",
                text, categories, paid, onlyAvailable, rangeStart, rangeEnd, sort, from, size);

        UserEventParams userEventParams = UserEventParams.builder()
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

        return ResponseEntity.ok(events);
    }

    /**
     * Возвращает информацию о конкретном событии по его ID.
     *
     * @param request   HTTP-запрос (используется для сбора статистики)
     * @param eventId   Идентификатор события
     * @return ResponseEntity с DTO события
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getPublishedEvent(
            HttpServletRequest request,
            @NotNull @PathVariable Long eventId
    ) {
        log.info("GET /events/{}", eventId);

        eventService.sendHit(request);
        EventDto eventDto = eventService.findPublishedEvent(eventId);
        log.info("Возвращено событие с ID={}", eventDto.getId());

        return ResponseEntity.ok(eventDto);
    }
}