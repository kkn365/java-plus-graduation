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

    private static final String USER_ID_HEADER = "X-EWM-USER-ID";

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

        List<EventDto> events = eventService.findAllByUserParams(userEventParams);
        log.info("Возвращено {} событий", events.size());

        return ResponseEntity.ok(events);
    }

    /**
     * Возвращает информацию о конкретном мероприятии по его идентификатору.
     * <p>
     * Метод извлекает идентификатор пользователя из HTTP-заголовка, вызывает сервис для получения данных о мероприятии,
     * логирует операцию и возвращает результат в виде объекта {@link EventDto}.
     *
     * @param userId   идентификатор пользователя, запрашивающего мероприятие
     * @param eventId  идентификатор мероприятия, которое необходимо получить
     * @return ResponseEntity с данными мероприятия и статусом 200 OK
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getPublishedEvent(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @NotNull @PathVariable Long eventId
    ) {
        log.info("GET /events/{} with request header: {}={}", eventId, USER_ID_HEADER, userId);

        EventDto eventDto = eventService.findPublishedEvent(eventId, userId);
        log.info("Возвращено событие с ID={} по запросу пользователя с ID={}", eventDto.getId(), userId);

        return ResponseEntity.ok(eventDto);
    }

    /**
     * Возвращает список рекомендуемых мероприятий для указанного пользователя.
     * <p>
     * Метод извлекает идентификатор пользователя из заголовка запроса, запрашивает рекомендации у сервиса,
     * логирует результат и возвращает его в виде JSON-списка объектов {@link EventDto}.
     *
     * @param userId идентификатор пользователя, для которого запрашиваются рекомендации
     * @return ResponseEntity со списком рекомендуемых мероприятий и статусом 200 OK
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<EventDto>> getRecommendations(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /events/recommendations with request header: {}={}", USER_ID_HEADER, userId);

        List<EventDto> recommendations = eventService.getRecommendations(userId);
        log.info("Возвращено {} рекомендаций для пользователя с ID={}", recommendations.size(), userId);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * Добавляет лайк к указанному мероприятию от имени пользователя.
     * <p>
     * Метод извлекает идентификатор пользователя из HTTP-заголовка, вызывает соответствующий метод сервиса,
     * и возвращает ответ без содержимого (204 No Content), если операция прошла успешно.
     *
     * @param userId   идентификатор пользователя, который ставит лайк
     * @param eventId  идентификатор мероприятия, которому ставится лайк
     * @return ResponseEntity с кодом 204 No Content
     */
    @PutMapping("/{eventId}/like")
    public ResponseEntity<Void> addLike(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long eventId
    ) {
        log.info("PUT /events/{}/like with request header: {}={}", eventId, USER_ID_HEADER, userId);

        eventService.addLike(eventId, userId);
        return ResponseEntity.noContent().build();
    }
}