package ru.practicum.core.event.service.impl;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.api.internal.request.client.RequestClient;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.event.dto.events.AdminEventParams;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.event.mapper.EventMapper;
import ru.practicum.core.event.model.Category;
import ru.practicum.core.event.model.Event;
import ru.practicum.core.event.model.enums.events.EventSortEnum;
import ru.practicum.core.event.model.enums.events.EventStateAction;
import ru.practicum.core.event.repository.EventRepository;
import ru.practicum.core.event.service.api.CategoryService;
import ru.practicum.core.event.service.api.EventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.CreateHitDTO;
import ru.practicum.stats.dto.HitsStatDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.core.event.model.enums.events.EventSortEnum.EVENT_DATE;
import static ru.practicum.core.event.repository.EventRepository.AdminEventSpec.withAdminParams;
import static ru.practicum.core.event.repository.EventRepository.UserEventSpec.withUserParams;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String EVENT_STATE_ACTION_CONFLICT_MESSAGE = "Можно изменять только события в состоянии ОЖИДАНИЕ или ОТМЕНЕНО";
    private static final String GET_EVENT_ERROR_MESSAGE = "Событие с ID=%d не найдено";
    private static final String EVENT_NOT_PUBLISHED_ERROR_MESSAGE = "Событие с ID=%d не опубликовано";
    private static final String EVENT_NOT_OWNED_BY_USER_ERROR_MESSAGE = "Событие с ID=%d не соответствует инициатору с ID=%d";
    private static final String PUBLISH_NOT_PENDING_EVENT_ERROR_MESSAGE = "Нельзя опубликовать событие, которое не находится в состоянии ОЖИДАНИЕ";
    private static final String REJECT_PUBLISHED_EVENT_ERROR_MESSAGE = "Нельзя отменить не опубликованное событие";
    private static final String UNACCEPTABLE_ACTION_ON_EVENT_ERROR_MESSAGE = "Недопустимое действие %s над состоянием события";
    private static final String EARLY_START_ERROR_MESSAGE = "Дата начала события не может быть раньше чем через один час после публикации";
    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "Пользователь с ID=%d не найден";
    private static final String USERS_NOT_FOUND_ERROR_MESSAGE = "Пользователи с ID=%s не найдены";
    private static final String PAGINATION_ERROR_MESSAGE = "Некорректные параметры пагинации";
    private static final String RANGE_ERROR_MESSAGE = "Некорректный диапазон";

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;

    private final StatsClient statsClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    /**
     * Метод добавления нового события.
     *
     * @param newEventDto DTO с данными нового события
     * @param userId      идентификатор пользователя, инициирующего событие
     * @return DTO созданного события
     * @throws NotFoundException если пользователь или категория не найдены
     */
    @Override
    public EventDto addEvent(NewEventDto newEventDto, Long userId) {
        // Получаем сущность пользователя из микросервиса user-service
        UserShortDto userShortDto = getUserOrThrow(userId);
        // Получаем сущность категории из сервиса
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        // Формируем модель события
        Event event = Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .category(category)
                .initiatorId(userShortDto.getId())
                .eventDate(newEventDto.getEventDate())
                .createdOn(LocalDateTime.now())
                .locationLat(newEventDto.getLocation().getLat())
                .locationLon(newEventDto.getLocation().getLon())
                .participantLimit(Objects.requireNonNullElse(newEventDto.getParticipantLimit(), 0))
                .paid(Objects.requireNonNullElse(newEventDto.getPaid(), false))
                .requestModeration(Objects.requireNonNullElse(newEventDto.getRequestModeration(), true))
                .state(EventState.PENDING)
                .build();

        // Сохраняем событие в репозитории
        Event savedEvent = eventRepository.save(event);

        // Преобразуем сохранённую модель в DTO
        EventDto savedEventDto = eventMapper.toDto(savedEvent);
        // Добавляем данные инициатора
        savedEventDto.setInitiator(userShortDto);
        log.info("Событие создано: {}", savedEventDto);
        return savedEventDto;
    }

    /**
     * Метод обновления события пользователем-инициатором.
     *
     * @param eventId      идентификатор события, которое требуется обновить
     * @param newEventDto  DTO с данными, которые необходимо изменить
     * @param userId       идентификатор пользователя, инициирующего обновление события
     * @return             DTO обновлённого события
     * @throws ConflictException если событие не находится в состоянии ОЖИДАНИЕ или ОТМЕНЕНО
     * @throws NotFoundException если событие или пользователь не найдены
     */
    @Override
    public EventDto updateEventByUser(Long eventId, UpdateEventUserRequest newEventDto, Long userId) {
        Event event = findEventById(eventId);
        validateInitiator(event, userId);

        if (!List.of(EventState.CANCELED, EventState.PENDING).contains(event.getState())) {
            throw new ConflictException(EVENT_STATE_ACTION_CONFLICT_MESSAGE);
        }

        return updateEvent(event, eventMapper.toNewEventDto(newEventDto));
    }

    /**
     * Метод обновления события администратором.
     *
     * @param eventId идентификатор события, которое необходимо обновить
     * @param newEventDto DTO с данными для обновления события
     * @return DTO обновлённого события
     * @throws ConflictException если действие над состоянием события невозможно
     * @throws NotFoundException если событие не найдено
     */
    @Override
    public EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest newEventDto) {
        // Получаем событие по ID
        Event event = findEventById(eventId);

        // Проверяем и обрабатываем действие над состоянием события
        if (newEventDto.getStateAction() != null) {
            validateAdminStateAction(newEventDto.getStateAction(), event);
        }

        return updateEvent(event, eventMapper.toNewEventDto(newEventDto));
    }

    /**
     * Метод получения списка событий, инициированных конкретным пользователем.
     *
     * @param userId идентификатор пользователя, чьи события требуется получить
     * @param from   начальная позиция для пагинации (количество пропускаемых записей)
     * @param size   количество возвращаемых записей
     * @return список DTO событий, инициированных пользователем
     * @throws ValidationException если параметры пагинации некорректны
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<EventDto> findAllByParams(Long userId, Integer from, Integer size) {
        // Создание объекта PageRequest для пагинации и сортировки
        PageRequest pageRequest = createPageRequest(from, size);

        // Получаем список событий из репозитория
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest)
                .stream()
                .toList();

        // Подготавливаем DTO событий с пользователями и статистикой
        return createEventDtoListWithAdditionalInfo(events);
    }

    /**
     * Метод получения информации о событии, инициированном конкретным пользователем.
     *
     * @param userId   идентификатор пользователя, который является инициатором события
     * @param eventId  идентификатор события, информацию о котором требуется получить
     * @return         DTO события с дополнительной информацией
     * @throws NotFoundException если событие или пользователь не найдены
     * @throws ConflictException       если событие не принадлежит указанному пользователю
     */
    @Override
    public EventDto findUserEvent(Long userId, Long eventId) {
        // Получаем событие по ID
        Event event = findEventById(eventId);

        // Проверяем, что событие принадлежит пользователю
        validateInitiator(event, userId);

        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    /**
     * Метод получения события по его идентификатору.
     *
     * @param eventId идентификатор события, которое необходимо найти
     * @return сущность события
     * @throws NotFoundException если событие с указанным идентификатором не найдено
     */
    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(GET_EVENT_ERROR_MESSAGE, eventId));
    }

    /**
     * Метод получения списка событий с фильтрацией по параметрам администратора.
     *
     * @param adminEventParams объект, содержащий параметры фильтрации и пагинации
     * @return список DTO событий с дополнительной информацией
     * @throws ValidationException если переданные параметры некорректны
     */
    @Override
    public List<EventDto> findAllByAdminParams(AdminEventParams adminEventParams) {
        // Проверяем корректность диапазона дат
        validateDateRange(adminEventParams.getRangeStart(), adminEventParams.getRangeEnd());

        // Создаём объект PageRequest
        PageRequest pageRequest = createPageRequest(
                adminEventParams.getFrom(),
                adminEventParams.getSize()
        );

        // Получение событий из репозитория по спецификации
        List<Event> events = eventRepository.findAll(withAdminParams(adminEventParams), pageRequest)
                .stream()
                .toList();

        // Подготавливаем DTO событий с пользователями и статистикой
        return createEventDtoListWithAdditionalInfo(events);
    }

    /**
     * Метод получения списка событий с фильтрацией по параметрам пользователя.
     *
     * @param userEventParams объект, содержащий параметры фильтрации, пагинации и сортировки
     * @return список DTO событий с дополнительной информацией
     * @throws ValidationException если переданные параметры некорректны
     */
    @Override
    public List<EventDto> findAllByUserParams(UserEventParams userEventParams) {
        log.debug("Получение событий с параметрами: {}", userEventParams);

        // Проверка корректности диапазона дат
        validateDateRange(userEventParams.getRangeStart(), userEventParams.getRangeEnd());

        // Создание объекта PageRequest для пагинации и сортировки
        PageRequest pageRequest = createPageRequest(
                userEventParams.getFrom(),
                userEventParams.getSize()
        );

        // Получение событий из репозитория по спецификации
        List<Event> events = eventRepository.findAll(withUserParams(userEventParams), pageRequest)
                .stream()
                .toList();

        // Получаем список DTO событий с пользователями и статистикой
        List<EventDto> eventDtos = createEventDtoListWithAdditionalInfo(events);

        // Фильтруем события по доступности (если требуется)
        if (Boolean.TRUE.equals(userEventParams.getOnlyAvailable())) {
            log.debug("Применена фильтрация по доступности");
            eventDtos = eventDtos.stream()
                    .filter(eventDto -> eventDto.getConfirmedRequests() < eventDto.getParticipantLimit())
                    .toList();
        }

        // Проверяем наличие и значение параметра сортировки
        if (userEventParams.getSort() != null
            && userEventParams.getSort().equals(EventSortEnum.VIEWS)) {
            log.debug("Применена сортировка по количеству просмотров");
            eventDtos.sort(Comparator.comparing(EventDto::getViews).reversed());
        }

        // Возвращаем список DTO событий
        return eventDtos;
    }

    /**
     * Метод для отправки информации о хите (просмотре) события в сервис статистики.
     * Используется для отслеживания количества просмотров событий.
     *
     * @param request объект HttpServletRequest, содержащий информацию о запросе клиента
     */
    @Override
    public void sendHit(HttpServletRequest request) {
        CreateHitDTO dto = CreateHitDTO
                .builder()
                .app("event-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.createHit(dto);
        log.debug("Отправлен hit: {}", dto);
    }

    /**
     * Метод получения информации о событии, которое находится в состоянии "Опубликовано".
     *
     * @param eventId идентификатор события, информацию о котором требуется получить
     * @return DTO события с дополнительной информацией
     * @throws NotFoundException если событие не найдено или не опубликовано
     */
    @Override
    public EventDto findPublishedEvent(Long eventId) {
        Event event = findEventById(eventId);

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException(String.format(EVENT_NOT_PUBLISHED_ERROR_MESSAGE, eventId));
        }
        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    /**
     * Метод получения DTO события по его идентификатору.
     * <p>
     *     Используется микросервисами.
     *
     * @param eventId идентификатор события, для которого необходимо получить DTO
     * @return DTO события с дополнительной информацией (пользователем и статистикой)
     * @throws NotFoundException если событие с указанным идентификатором не найдено
     */
    @Override
    public EventDto findEventDtoById(Long eventId) {
        Event event = findEventById(eventId);
        return createEventDtoWithAdditionalInfo(event);
    }

    /**
     * Метод получения списка событий, инициированных конкретным пользователем.
     * <p>
     *     Используется микросервисами.
     *
     * @param initiatorId идентификатор пользователя-инициатора событий
     * @return список DTO событий с дополнительной информацией (пользователями и статистикой)
     * @throws NotFoundException если пользователь с указанным идентификатором не найден
     */
    @Override
    public List<EventDto> findAllEventsByInitiatorId(Long initiatorId) {
        // Получение событий из репозитория
        List<Event> events = eventRepository.findAllByInitiatorId(initiatorId).stream().toList();
        // Возвращаем список DTO событий
        return createEventDtoListWithAdditionalInfo(events);
    }

    /**
     * Метод создания DTO события с дополнительной информацией (пользователем и статистикой).
     *
     * @param event событие, для которого необходимо создать DTO
     * @return DTO события с полной информацией
     */
    private EventDto createEventDtoWithAdditionalInfo(Event event) {
        // Возвращаем DTO события
        return createEventDtoListWithAdditionalInfo(List.of(event)).getFirst();
    }

    /**
     * Метод преобразования списка событий в список DTO с дополнительной информацией:
     * инициатор события, количество подтверждённых заявок и статистика просмотров.
     *
     * @param events список событий, которые необходимо преобразовать
     * @return список DTO событий с дополнительной информацией (пользователями и статистикой)
     * @throws NotFoundException если пользователь-инициатор не найден
     */
    private List<EventDto> createEventDtoListWithAdditionalInfo(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of(); // Нет событий — ничего не делать
        }
        // Получаем список уникальных ID инициаторов событий
        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();
        // Получаем пользователей по ID
        Map<Long, UserShortDto> users = getUsersOrThrow(initiatorIds);

        // Запрашиваем количество подтверждённых заявок
        Map<Long, Long> confirmedRequestsCount = loadConfirmedRequestsCount(events);

        // Преобразуем события в DTO с дополнительной информацией
        List<EventDto> eventDtos = events.stream()
                .map(event -> {
                    EventDto dto = eventMapper.toDto(event);
                    dto.setInitiator(users.get(event.getInitiatorId()));
                    dto.setConfirmedRequests(confirmedRequestsCount.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();

        // Получаем минимальную и максимальную дату событий
        LocalDateTime earliestCreatedOn = getEarliestCreatedOn(events);
        LocalDateTime latestEventDate = getLatestEventDate(events, earliestCreatedOn);

        // Добавляем статистику просмотров
        loadViews(eventDtos, earliestCreatedOn, latestEventDate);

        // Возвращаем список DTO событий c с дополнительной информацией
        return eventDtos;
    }

    /**
     * Метод загрузки статистики просмотров (views) для списка событий.
     * Запрашивает данные у сервиса статистики и обновляет соответствующие DTO событий.
     *
     * @param events Список DTO событий, для которых нужно получить статистику просмотров
     * @param start  Начальная дата диапазона для получения статистики.
     *               Может быть null — тогда используется текущая дата минус 1 час
     * @param end    Конечная дата диапазона для получения статистики.
     *               Может быть null — тогда используется текущая дата
     */
    private void loadViews(List<EventDto> events, LocalDateTime start, LocalDateTime end) {
        // Создаём маппинг между ID события и URI для запроса статистики
        Map<Long, String> eventUriMap = events.stream()
                .collect(Collectors.toMap(
                        EventDto::getId,
                        event -> "/events/" + event.getId(), // Формат URI согласно требованиям статистики
                        (existing, replacement) -> existing)); // Обработка дубликатов (должно не случаться)

        try {
            // Получаем статистику просмотров из внешнего сервиса
            ResponseEntity<List<HitsStatDTO>> statsResponse = statsClient.getStats(
                    start == null ? LocalDateTime.now().minusHours(1L) : start,
                    end == null ? LocalDateTime.now() : end,
                    List.copyOf(eventUriMap.values()), // Гарантируем неизменяемость списка
                    true // Учитываем уникальные IP-адреса (статистика по уникальным просмотрам)
            );

            // Если данные получены, создаём маппинг URI → количество просмотров
            if (statsResponse.hasBody()) {
                List<HitsStatDTO> stats = statsResponse.getBody();
                if (stats != null && !stats.isEmpty()) {
                    Map<String, Long> uriToHits = stats.stream()
                            .collect(Collectors.toMap(HitsStatDTO::getUri, HitsStatDTO::getHits));

                    // Обновляем DTO событий значениями статистики
                    for (EventDto event : events) {
                        String uri = eventUriMap.get(event.getId());
                        event.setViews(uriToHits.getOrDefault(uri, 0L));
                    }
                    return;
                }
            }
            // Если данных нет или тело пустое, устанавливаем просмотры в 0 для всех событий
            events.forEach(event -> event.setViews(0L));
        } catch (FeignException e) {
            log.error("Ошибка при получении статистики просмотров: {}", e.getMessage(), e);
            // В случае ошибки оставляем текущие значения views без изменений
        }
    }

    /**
     * Метод получения количества подтверждённых заявок на участие в событиях.
     *
     * @param events список событий, для которых необходимо получить количество подтверждённых заявок
     * @return маппинг: идентификатор события → количество подтверждённых заявок
     * @throws FeignException при ошибке запроса к сервису заявок
     */
    private Map<Long, Long> loadConfirmedRequestsCount(List<Event> events) {
        // Извлечение уникальных идентификаторов событий
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        log.debug("Загрузка количества подтверждённых заявок для событий: {}", eventIds);

        try {
            ResponseEntity<List<EventRequestsCountDto>> response = requestClient.getEventRequestsCount(eventIds);

            // Проверка успешности запроса и наличия данных
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Неуспешный ответ от сервиса заявок: HTTP {}", response.getStatusCode());
                return Collections.emptyMap();
            }

            if (response.getBody() == null) {
                log.warn("Ответ от сервиса заявок не содержит тело для событий: {}", eventIds);
                return Collections.emptyMap();
            }

            // Преобразование данных в маппинг
            return createConfirmedRequestsMap(response.getBody());

        } catch (FeignException fe) {
            log.error("Ошибка при получении количества подтверждённых заявок для событий {}: {}",
                    eventIds, fe.getMessage(), fe);
            return Collections.emptyMap();
        }
    }

    /**
     * Метод преобразует список DTO с количеством подтверждённых заявок на события в маппинг:
     * идентификатор события → количество подтверждённых заявок.
     *
     * @param countDtos список объектов EventRequestsCountDto, содержащих информацию о количестве
     *                  подтверждённых заявок для каждого события
     * @return маппинг: ключ — идентификатор события, значение — количество подтверждённых заявок
     */
    private Map<Long, Long> createConfirmedRequestsMap(List<EventRequestsCountDto> countDtos) {
        return countDtos.stream()
                .collect(Collectors.toMap(
                        EventRequestsCountDto::getEventId,
                        EventRequestsCountDto::getConfirmedRequests
                ));
    }

    /**
     * Проверяет, является ли указанный пользователь инициатором события.
     *
     * @param event  событие, для которого проверяется инициатор
     * @param userId идентификатор пользователя, который должен быть инициатором события
     * @throws ConflictException если пользователь не является инициатором события
     */
    private void validateInitiator(Event event, Long userId) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException(String.format(EVENT_NOT_OWNED_BY_USER_ERROR_MESSAGE, event.getId(), userId));
        }
    }

    /**
     * Метод проверяет, допустимо ли выполнение указанного действия над состоянием события.
     * Выполняет валидацию на основе текущего состояния события и запрашиваемого действия.
     *
     * @param action действие над состоянием события (PUBLISH_EVENT, REJECT_EVENT)
     * @param event событие, состояние которого необходимо изменить
     * @throws ConflictException если действие невозможно из-за текущего состояния события или неверного типа действия
     */
    private void validateAdminStateAction(EventStateAction action, Event event) {
        switch (action) {
            case PUBLISH_EVENT:
                if (!EventState.PENDING.equals(event.getState())) {
                    throw new ConflictException(PUBLISH_NOT_PENDING_EVENT_ERROR_MESSAGE);
                }
                validatePublishDate(event);
                break;
            case REJECT_EVENT:
                if (EventState.PUBLISHED.equals(event.getState())) {
                    throw new ConflictException(REJECT_PUBLISHED_EVENT_ERROR_MESSAGE);
                }
                break;
            default:
                throw new ConflictException(UNACCEPTABLE_ACTION_ON_EVENT_ERROR_MESSAGE, action);
        }
    }

    /**
     * Метод проверяет, что дата начала события не наступает раньше чем через один час от текущего времени.
     * Используется при публикации события администратором.
     *
     * @param event событие, для которого проверяется дата начала
     * @throws ConflictException если дата начала события наступает раньше чем через один час
     */
    private void validatePublishDate(Event event) {
        LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1L);
        if (nowPlusHour.isAfter(event.getEventDate())) {
            throw new ConflictException(EARLY_START_ERROR_MESSAGE);
        }
    }

    /**
     * Метод обновления данных события.
     *
     * @param event   событие, которое необходимо обновить
     * @param request DTO с новыми данными события
     * @return DTO обновлённого события
     * @throws ConflictException если дата начала события наступает раньше чем через один час после публикации
     */
    private EventDto updateEvent(Event event, NewEventDto request) {
        if (EventStateAction.PUBLISH_EVENT.equals(request.getStateAction())) {
            LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1L);
            LocalDateTime eventDate = request.getEventDate() != null ? request.getEventDate() : event.getEventDate();

            if (nowPlusHour.isAfter(eventDate)) {
                throw new ConflictException(EARLY_START_ERROR_MESSAGE);
            }
        }
        // Обновляем поля события
        updateEventFields(event, request);
        // Сохраняем изменения в репозитории
        eventRepository.save(event);
        log.info("Событие изменено: {}", event);
        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    /**
     * Метод получения информации о пользователе по его идентификатору.
     * Выполняет запрос к внешнему сервису пользователей (user-service).
     *
     * @param userId идентификатор пользователя, информацию о котором необходимо получить
     * @return объект UserShortDto с краткой информацией о пользователе
     * @throws NotFoundException если пользователь не найден или при ошибке обращения к сервису
     * @throws FeignException    при сетевой или HTTP-ошибке при обращении к user-service
     */
    private UserShortDto getUserOrThrow(long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка наличия тела ответа
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }

            // Если статус успешный, но тело отсутствует
            throw new NotFoundException(String.format(USER_NOT_FOUND_ERROR_MESSAGE, userId));

        } catch (FeignException fe) {
            log.error("Ошибка запроса к пользовательскому сервису: {} [HTTP {}: {}]",
                    userId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USER_NOT_FOUND_ERROR_MESSAGE, userId), fe);
        }
    }

    /**
     * Метод получения информации о нескольких пользователях по их идентификаторам.
     * Выполняет запрос к внешнему сервису пользователей (user-service).
     *
     * @param userIds список идентификаторов пользователей, информацию о которых необходимо получить
     * @return Map, где ключ — ID пользователя, значение — DTO с краткой информацией о пользователе
     * @throws NotFoundException если хотя бы один пользователь не найден или при ошибке обращения к сервису
     * @throws FeignException    при сетевой или HTTP-ошибке при обращении к user-service
     */
    private Map<Long, UserShortDto> getUsersOrThrow(List<Long> userIds) {
        try {
            ResponseEntity<List<UserShortDto>> response = userClient.getUsers(userIds);

            // Проверка успешного статуса ответа
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Пользовательский сервис вернул статус {}: {}", response.getStatusCode(), response.getHeaders());
                throw new NotFoundException(String.format(USERS_NOT_FOUND_ERROR_MESSAGE, userIds));
            }

            // Проверка наличия тела ответа
            List<UserShortDto> users = response.getBody();
            if (users == null || users.isEmpty()) {
                log.warn("Ответ от пользовательского сервиса пуст для пользователей {}", userIds);
                throw new NotFoundException(String.format(USERS_NOT_FOUND_ERROR_MESSAGE, userIds));
            }

            return users.stream()
                    .collect(Collectors.toMap(
                            UserShortDto::getId,
                            user -> user,
                            (existing, replacement) -> existing // Обработка дубликатов (ожидается, что их нет)
                    ));

        } catch (FeignException fe) {
            log.error("Ошибка при получении пользователей {}: HTTP {} - {}",
                    userIds, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USERS_NOT_FOUND_ERROR_MESSAGE, userIds), fe);
        }
    }

    /**
     * Метод определяет самую раннюю дату создания события из списка событий.
     * Используется для установления временного диапазона при запросе статистики просмотров.
     *
     * @param events список событий, для которых определяется минимальная дата создания
     * @return самая ранняя дата создания события или текущее время, если даты отсутствуют
     */
    private LocalDateTime getEarliestCreatedOn(List<Event> events) {
        return events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    /**
     * Метод определяет самую позднюю дату события из списка событий.
     * Используется для установления временного диапазона при запросе статистики просмотров.
     *
     * @param events               список событий, для которых определяется максимальная дата события
     * @param earliestCreatedOn    минимальная дата создания события (используется как fallback)
     * @return                     самая поздняя дата события или fallback-значение, если даты отсутствуют
     */
    private LocalDateTime getLatestEventDate(List<Event> events, LocalDateTime earliestCreatedOn) {
        return events.stream()
                .map(Event::getEventDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(earliestCreatedOn.plusDays(1));
    }

    /**
     * Метод обновляет поля события на основе данных из DTO.
     * Обновление происходит только для тех полей, которые не равны null в объекте NewEventDto.
     * Также выполняется логика изменения состояния события в зависимости от переданного действия.
     *
     * @param event   событие, которое необходимо обновить
     * @param request DTO с данными для обновления события
     */
    private void updateEventFields(Event event, NewEventDto request) {
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocationLat(request.getLocation().getLat());
            event.setLocationLon(request.getLocation().getLon());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT, CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
    }

    /**
     * Метод создаёт объект PageRequest для пагинации и сортировки.
     * Используется при получении списка событий.
     *
     * @param from параметр, указывающий начальную позицию (сколько записей пропустить)
     * @param size количество возвращаемых записей на странице
     * @return объект PageRequest, настроенный на пагинацию и сортировку по дате события по возрастанию
     * @throws ValidationException если параметры from или size некорректны
     */
    private PageRequest createPageRequest(int from, int size) {
        // Проверяем параметры пагинации
        if (from < 0 || size <= 0) {
            throw new ValidationException(PAGINATION_ERROR_MESSAGE);
        }

        int page = from / size;

        return PageRequest.of(
                page,
                size,
                Sort.by(EVENT_DATE.getSortField()).ascending()
        );
    }

    /**
     * Метод валидации временного диапазона: проверяет, что дата окончания (end) находится после даты начала (start).
     * Используется при фильтрации событий по временным параметрам.
     *
     * @param start начальная дата диапазона
     * @param end   конечная дата диапазона
     * @throws ValidationException если end не позже start, т.е. если диапазон некорректен
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException(RANGE_ERROR_MESSAGE);
        }
    }
}
