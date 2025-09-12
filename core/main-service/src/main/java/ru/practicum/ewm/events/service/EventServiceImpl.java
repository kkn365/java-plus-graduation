package ru.practicum.ewm.events.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.categories.service.CategoryService;
import ru.practicum.ewm.events.dto.AdminEventParams;
import ru.practicum.ewm.events.dto.EventDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.events.dto.UpdateEventUserRequest;
import ru.practicum.ewm.events.dto.UserEventParams;
import ru.practicum.ewm.events.enumeration.EventSortEnum;
import ru.practicum.ewm.events.enumeration.EventState;
import ru.practicum.ewm.events.enumeration.EventStateAction;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.users.model.EventRequestCount;
import ru.practicum.ewm.users.model.RequestStatus;
import ru.practicum.ewm.users.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.repository.EventRepository.AdminEventSpec.withAdminParams;
import static ru.practicum.ewm.events.repository.EventRepository.UserEventSpec.withUserParams;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final RequestRepository requestRepository;

    private final StatsClient statsClient;
    private final UserClient userClient;

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден!";

    /**
     * Создаёт новое событие на основе данных из DTO и идентификатора пользователя.
     *
     * @param newEventDto данные нового события
     * @param userId      идентификатор пользователя (инициатор события)
     * @return DTO созданного события
     */
    @Override
    public EventDto addEvent(NewEventDto newEventDto, Long userId) {
        // Получаем сущность пользователя из микросервиса user-service
        UserShortDto userShortDto = getUserOrThrow(userId);
        // Получаем сущность категории из сервиса
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        // Преобразуем DTO в модель события
        Event event = eventMapper.toModel(newEventDto);

        // Устанавливаем значения по умолчанию для необязательных полей
        event.setInitiatorId(userId);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(Objects.requireNonNullElse(newEventDto.getPaid(), false));
        event.setParticipantLimit(Objects.requireNonNullElse(newEventDto.getParticipantLimit(), 0));
        event.setRequestModeration(Objects.requireNonNullElse(newEventDto.getRequestModeration(), true));
        event.setState(EventState.PENDING);

        // Сохраняем событие в репозитории
        Event savedEvent = eventRepository.save(event);

        // Преобразуем сохранённую модель обратно в DTO
        EventDto savedEventDto = eventMapper.toDto(savedEvent);
        // Добавляем данные инициатора
        savedEventDto.setInitiator(userShortDto);
        log.info("Событие создано: {}", savedEventDto);
        return savedEventDto;
    }

    /**
     * Обновляет событие пользователем-инициатором.
     * <p>
     * Проверяет, что событие принадлежит пользователю и находится в состоянии ОЖИДАНИЕ или ОТМЕНЕНО.
     *
     * @param eventId     Идентификатор события
     * @param newEventDto Данные для обновления события
     * @param userId      Идентификатор пользователя (инициатор)
     * @return DTO обновлённого события
     */
    @Override
    public EventDto updateEventByUser(Long eventId, UpdateEventUserRequest newEventDto, Long userId) {
        Event event = findEventById(eventId);
        validateInitiator(event, userId);

        if (!List.of(EventState.CANCELED, EventState.PENDING).contains(event.getState())) {
            throw new ConflictException("Можно изменять только события в состоянии ОЖИДАНИЕ или ОТМЕНЕНО");
        }

        return updateEvent(event, eventMapper.toNewEventDto(newEventDto));
    }

    /**
     * Обновляет событие администратором.
     * <p>
     * Позволяет изменять состояние события (ОПУБЛИКОВАТЬ, ОТКЛОНИТЬ) и другие параметры.
     * Выполняются проверки на корректность действий и текущего состояния события.
     *
     * @param eventId     Идентификатор события
     * @param newEventDto DTO с новыми данными события
     * @return Обновлённое событие в виде DTO
     * @throws ConflictException если действие или состояние события некорректны
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
     * Возвращает список событий, принадлежащих указанному пользователю, с пагинацией.
     * <p>
     * Метод выполняет следующие шаги:
     * 1. Проверяет существование пользователя.
     * 2. Получает события из репозитория с учётом пагинации (from, size).
     * 3. Загружает информацию об инициаторах событий из внешнего сервиса.
     * 4. Преобразует события в DTO-объекты.
     * 5. Загружает статистику просмотров и количество подтверждённых заявок.
     * 6. Возвращает результат.
     *
     * @param userId Идентификатор пользователя, чьи события необходимо получить.
     * @param from   Начальная позиция (смещение) для пагинации. Должно быть &gt;= 0.
     * @param size   Количество событий на странице. Должно быть &gt; 0.
     * @return Список DTO событий с информацией об инициаторах, статистикой просмотров и заявками.
     * @throws NotFoundException Если пользователь с указанным ID не существует.
     */
    @Override
    public List<EventDto> findAllByParams(Long userId, Integer from, Integer size) {
        // Проверяем параметры пагинации
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }

        validateUserExists(userId);
        int page = from / size;

        // Получаем список событий из репозитория
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(page, size))
                .stream()
                .toList();

        // Подготавливаем DTO событий с пользователями и статистикой
        return prepareEventDtosWithUsers(events);
    }

    /**
     * Возвращает событие в виде DTO для указанного пользователя.
     * <p>
     * Метод проверяет, что событие принадлежит пользователю, и загружает дополнительную статистику.
     *
     * @param userId  Идентификатор пользователя (инициатор события)
     * @param eventId Идентификатор события
     * @return DTO события с полной информацией
     */
    @Override
    public EventDto findUserEvent(Long userId, Long eventId) {
        // Получаем событие по ID
        Event event = findEventById(eventId);

        // Проверяем, что событие принадлежит пользователю
        validateInitiator(event, userId);

        // Возвращаем DTO события с дополнительной статистикой и информацией о пользователе
        return createEventDtoWithStats(event);
    }

    /**
     * Ищет событие по его идентификатору.
     * <p>
     * Если событие не найдено, выбрасывается NotFoundException.
     *
     * @param eventId Идентификатор события
     * @return Найденное событие
     * @throws NotFoundException если событие с указанным ID не существует
     */
    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));
    }

    /**
     * Получает список событий, соответствующих параметрам администратора.
     * <p>
     * Метод выполняет фильтрацию и пагинацию событий на основе переданных параметров.
     * Также подгружает статистику просмотров и количество подтверждённых заявок.
     *
     * @param adminEventParams Параметры запроса: список пользователей, список категорий,
     *                         диапазон дат, список статусов событий, пагинация
     * @return Список DTO событий, соответствующих критериям
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
        return prepareEventDtosWithUsers(events);
    }

    /**
     * Получает список событий, соответствующих параметрам пользователя.
     * <p>
     * Метод выполняет фильтрацию и пагинацию событий на основе переданных параметров.
     * Также подгружает статистику просмотров и количество подтверждённых заявок.
     * Если указано, результат может быть отсортирован по количеству просмотров.
     *
     * @param userEventParams Параметры запроса: текст для поиска, категория, диапазон дат, флаг платности,
     *                        флаг доступности, сортировка, пагинация
     * @return Список DTO событий, соответствующих критериям
     * @throws ValidationException если диапазон дат некорректен (rangeEnd <= rangeStart)
     */
    @Override
    public List<EventDto> findAllByUserParams(UserEventParams userEventParams) {
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
        List<EventDto> eventDtos = prepareEventDtosWithUsers(events);

        // Проверяем наличие и значение параметра сортировки
        if (userEventParams.getSort() != null
            && userEventParams.getSort().equals(EventSortEnum.VIEWS)) {
            eventDtos.sort(Comparator.comparing(EventDto::getViews).reversed());
        }

        // Возвращаем список DTO событий
        return eventDtos;
    }

    /**
     * Отправляет информацию о хите (просмотре) в сервис статистики.
     * <p>
     * Формирует DTO с данными запроса и передаёт его клиенту для обработки.
     *
     * @param request Объект HTTP-запроса, из которого извлекаются IP и URI
     */
    @Override
    public void sendHit(HttpServletRequest request) {
        CreateHitDTO dto = CreateHitDTO
                .builder()
                .app("main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.createHit(dto);
        log.debug("Отправлен hit: {}", dto);
    }

    /**
     * Возвращает DTO события, если оно находится в состоянии ПУБЛИКОВАНО.
     * <p>
     * Если событие не найдено или его статус отличен от ПУБЛИКОВАНО, выбрасывается NotFoundException.
     *
     * @param eventId Идентификатор события
     * @return DTO события
     */
    @Override
    public EventDto findPublishedEvent(Long eventId) {
        Event event = findEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие с ID=" + eventId + " не найдено или не опубликовано");
        }
        // Возвращаем DTO события
        return createEventDtoWithStats(event);
    }

    /**
     * Проверяет, что событие принадлежит указанному пользователю.
     * <p>
     * Если инициатор события не совпадает с переданным userId, выбрасывается ConflictException.
     *
     * @param event  Событие, которое нужно проверить
     * @param userId Идентификатор пользователя, который должен быть инициатором события
     */
    private void validateInitiator(Event event, Long userId) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Событие с ID "
                                        + event.getId()
                                        + " не соответствует инициатору "
                                        + userId);
        }
    }

    /**
     * Проверяет корректность действия над состоянием события для администратора.
     * <p>
     * Выполняет валидацию на основе текущего состояния события и запрашиваемого действия.
     *
     * @param action Действие над состоянием события
     * @param event  Текущее событие
     * @throws ConflictException если действие или состояние события некорректны
     */
    private void validateAdminStateAction(EventStateAction action, Event event) {
        switch (action) {
            case PUBLISH_EVENT:
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Нельзя изменить состояние события, так как оно не находится " +
                                                "в правильном состоянии: ОЖИДАНИЕ");
                }
                validatePublishDate(event);
                break;
            case REJECT_EVENT:
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConflictException("Нельзя изменить состояние события, так как оно не находится " +
                                                "в правильном состоянии: ОПУБЛИКОВАНО");
                }
                break;
            default:
                throw new ConflictException("Недопустимое действие: " + action);
        }
    }

    /**
     * Проверяет, что дата начала события не ранее чем через один час после публикации.
     * <p>
     * Согласно требованиям, событие не может быть опубликовано, если его начало менее чем через час.
     *
     * @param event Текущее событие
     * @throws ConflictException если дата начала события некорректна
     */
    private void validatePublishDate(Event event) {
        LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1L);
        if (nowPlusHour.isAfter(event.getEventDate())) {
            throw new ConflictException("Дата начала события не может быть раньше чем через один час после публикации");
        }
    }

    /**
     * Загружает количество подтверждённых заявок для переданных событий.
     * <p>
     * Использует репозиторий заявок, чтобы получить данные о количестве подтверждённых заявок по каждому событию.
     *
     * @param events Список DTO событий, для которых нужно загрузить количество подтверждённых заявок
     */
    private void loadConfirmedRequests(List<EventDto> events) {
        if (events.isEmpty()) {
            return; // Нет событий — ничего не делать
        }

        // Получаем ID всех событий из DTO
        List<Long> eventIds = events.stream()
                .map(EventDto::getId)
                .toList();

        // Запрашиваем количество подтверждённых заявок по каждому событию
        List<EventRequestCount> confirmedRequests = requestRepository.countByStatusForEvents(eventIds, RequestStatus.CONFIRMED);

        // Создаём маппинг: ID события → количество подтверждённых заявок
        Map<Long, Long> eventIdToRequestsCount = confirmedRequests.stream()
                .collect(Collectors.toMap(
                        EventRequestCount::eventId,
                        EventRequestCount::requestsCount
                ));

        // Обновляем DTO событий значениями количества подтверждённых заявок
        for (EventDto event : events) {
            Long count = eventIdToRequestsCount.getOrDefault(event.getId(), 0L);
            event.setConfirmedRequests(Math.toIntExact(count));
        }
    }

    /**
     * Загружает статистику просмотров (views) для переданных событий за указанный период.
     * <p>
     * Использует клиент статистики (statsClient), чтобы получить количество просмотров по URI событий.
     *
     * @param events Список DTO событий, для которых нужно загрузить статистику
     * @param start  Начальная дата диапазона для подсчёта просмотров
     * @param end    Конечная дата диапазона для подсчёта просмотров
     */
    private void loadViews(List<EventDto> events, LocalDateTime start, LocalDateTime end) {
        if (events.isEmpty()) {
            return; // Нет событий — ничего не делать
        }

        // Создаём маппинг между ID события и URI для запроса статистики
        Map<Long, String> eventUriMap = events.stream()
                .collect(Collectors.toMap(
                        EventDto::getId,
                        event -> "/events/" + event.getId(), // Формат URI согласно требованиям статистики
                        (existing, replacement) -> existing)); // Обработка дубликатов (должно не случаться)

        try {
            // Получаем статистику просмотров из внешнего сервиса
            ResponseEntity<List<HitsStatDTO>> statsResponse = statsClient.getStats(
                    start,
                    end,
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
     * Обновляет событие на основе переданных данных.
     * <p>
     * Метод выполняет проверку корректности действий над состоянием события,
     * обновляет поля, если они указаны в DTO, и сохраняет изменения в репозитории.
     * После обновления загружается статистика просмотров и количество подтверждённых заявок.
     *
     * @param event Сущность события, которую нужно обновить
     * @param dto   DTO с новыми данными события
     * @return Обновлённое событие в виде DTO
     * @throws ConflictException если дата начала события не соответствует требованиям
     */
    private EventDto updateEvent(Event event, NewEventDto dto) {
        if (EventStateAction.PUBLISH_EVENT.equals(dto.getStateAction())) {
            LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1L);
            LocalDateTime eventDate = dto.getEventDate() != null ? dto.getEventDate() : event.getEventDate();

            if (nowPlusHour.isAfter(eventDate)) {
                throw new ConflictException("Дата начала события не может быть ранее чем через один час после публикации");
            }
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            event.setLocationLat(dto.getLocation().getLat());
            event.setLocationLon(dto.getLocation().getLon());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case REJECT_EVENT, CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }

        eventRepository.save(event);
        log.info("Событие изменено: {}", event);

        // Получаем сущность пользователя из микросервиса user-service
        UserShortDto userShortDto = getUserOrThrow(event.getInitiatorId());
        // Преобразуем модель события в DTO
        EventDto eventDto = eventMapper.toDto(event, userShortDto);
        // Загружаем статистику просмотров и количество подтверждённых заявок
        loadEventStatistics(List.of(eventDto), event.getPublishedOn(), event.getEventDate());
        // Возвращаем DTO события
        return eventDto;
    }

    /**
     * Проверяет существование пользователя с указанным идентификатором.
     * <p>
     * Выполняет запрос к пользовательскому сервису через Feign-клиент.
     * Если пользователь не найден или произошла ошибка, выбрасывается {@link NotFoundException}.
     *
     * @param userId уникальный идентификатор пользователя
     * @throws NotFoundException если пользователь не найден или произошла ошибка при получении данных
     */
    private void validateUserExists(long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка успешного статуса ответа и наличия тела
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Пользовательский сервис вернул статус {}: {}",
                        response.getStatusCode(),
                        response.getHeaders());
                throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
            }

            if (response.getBody() == null) {
                log.warn("Ответ от пользовательского сервиса не содержит тело для пользователя ID={}", userId);
                throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
            }

        } catch (FeignException fe) {
            log.error("Ошибка при проверке существования пользователя ID={}: HTTP {} - {}",
                    userId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId), fe);
        }
    }

    /**
     * Получает краткую информацию о пользователе по его идентификатору.
     * <p>
     * В случае отсутствия пользователя или ошибки внешнего сервиса выбрасывает {@link NotFoundException}.
     *
     * @param userId уникальный идентификатор пользователя
     * @return DTO с краткой информацией о пользователе
     * @throws NotFoundException если пользователь не найден или произошла ошибка при получении данных
     */
    private UserShortDto getUserOrThrow(long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка наличия тела ответа
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }

            // Если статус успешный, но тело отсутствует
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));

        } catch (FeignException fe) {
            log.error("Ошибка запроса к пользовательскому сервису: {} [HTTP {}: {}]",
                    userId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId), fe);
        }
    }

    /**
     * Получает информацию о пользователях по их идентификаторам.
     * <p>
     * Выполняет запрос к пользовательскому сервису через Feign-клиент.
     * Если хотя бы один пользователь не найден или произошла ошибка, выбрасывается {@link NotFoundException}.
     *
     * @param userIds Список уникальных идентификаторов пользователей
     * @return Карта (ID пользователя → краткая информация о пользователе)
     * @throws NotFoundException если хотя бы один пользователь не найден или произошла ошибка при получении данных
     */
    private Map<Long, UserShortDto> getUsersOrThrow(List<Long> userIds) {
        try {
            ResponseEntity<List<UserShortDto>> response = userClient.getUsers(userIds);

            // Проверка успешного статуса ответа
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Пользовательский сервис вернул статус {}: {}", response.getStatusCode(), response.getHeaders());
                throw new NotFoundException(String.format("Пользователи %s не найдены", userIds));
            }

            // Проверка наличия тела ответа
            List<UserShortDto> users = response.getBody();
            if (users == null || users.isEmpty()) {
                log.warn("Ответ от пользовательского сервиса пуст для пользователей {}", userIds);
                throw new NotFoundException(String.format("Пользователи %s не найдены", userIds));
            }

            // Построение карты ID → UserShortDto
            return users.stream()
                    .collect(Collectors.toMap(
                            UserShortDto::getId,
                            user -> user,
                            (existing, replacement) -> existing // Обработка дубликатов (ожидается, что их нет)
                    ));

        } catch (FeignException fe) {
            log.error("Ошибка при получении пользователей {}: HTTP {} - {}",
                    userIds, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format("Пользователи %s не найдены", userIds), fe);
        }
    }

    /**
     * Загружает статистику просмотров и количество подтверждённых заявок для списка событий.
     *
     * @param eventDtos Список DTO событий
     * @param start     Начальная дата диапазона
     * @param end       Конечная дата диапазона
     */
    private void loadEventStatistics(List<EventDto> eventDtos, LocalDateTime start, LocalDateTime end) {
        try {
            loadViews(eventDtos, start, end);
            loadConfirmedRequests(eventDtos);
        } catch (Exception e) {
            log.warn("Не удалось загрузить статистику просмотров и заявок", e);
        }
    }

    /**
     * Проверяет, содержит ли список событий элементы.
     *
     * @param events Список событий
     * @return true, если список не пустой, иначе false
     */
    private boolean hasEvents(List<Event> events) {
        return events != null && !events.isEmpty();
    }

    /**
     * Возвращает список уникальных ID инициаторов событий.
     *
     * @param events Список событий
     * @return Список уникальных ID инициаторов
     */
    private List<Long> getUniqueInitiatorIds(List<Event> events) {
        return events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();
    }

    /**
     * Подготавливает события к выводу: получает пользователей, преобразует в DTO и загружает статистику.
     *
     * @param events Список событий
     * @return Список DTO событий с полной информацией
     */
    private List<EventDto> prepareEventDtosWithUsers(List<Event> events) {
        if (!hasEvents(events)) {
            return List.of();
        }

        List<Long> initiatorIds = getUniqueInitiatorIds(events);
        Map<Long, UserShortDto> users = getUsersOrThrow(initiatorIds);

        List<EventDto> eventDtos = events.stream()
                .map(event -> eventMapper.toDto(event, users.get(event.getInitiatorId())))
                .toList();

        LocalDateTime earliestCreatedOn = getEarliestCreatedOn(eventDtos);
        LocalDateTime latestEventDate = getLatestEventDate(eventDtos, earliestCreatedOn);

        loadEventStatistics(eventDtos, earliestCreatedOn, latestEventDate);
        return eventDtos;
    }

    private LocalDateTime getLatestEventDate(List<EventDto> eventDtos, LocalDateTime earliestCreatedOn) {
        return eventDtos.stream()
                .map(EventDto::getEventDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(earliestCreatedOn.plusDays(1));
    }

    private LocalDateTime getEarliestCreatedOn(List<EventDto> eventDtos) {
        return eventDtos.stream()
                .map(EventDto::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    /**
     * Проверяет корректность диапазона дат.
     *
     * @param start Начальная дата
     * @param end   Конечная дата
     * @throws ValidationException если end <= start
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException("rangeEnd должен быть позже, чем rangeStart");
        }
    }

    /**
     * Обновляет поля события на основе данных из DTO.
     *
     * @param event Сущность события
     * @param dto   DTO с новыми данными
     */
    private void updateEventFields(Event event, NewEventDto dto) {
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            event.setLocationLat(dto.getLocation().getLat());
            event.setLocationLon(dto.getLocation().getLon());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
    }

    /**
     * Создаёт DTO события с полной информацией о пользователе и статистикой.
     *
     * @param event Сущность события
     * @return DTO события
     */
    private EventDto createEventDtoWithStats(Event event) {
        UserShortDto userShortDto = getUserOrThrow(event.getInitiatorId());
        EventDto eventDto = eventMapper.toDto(event, userShortDto);
        loadEventStatistics(List.of(eventDto), event.getPublishedOn(), event.getEventDate());
        return eventDto;
    }

    /**
     * Создаёт объект PageRequest для пагинации и сортировки.
     *
     * @param from  Начальная позиция (смещение)
     * @param size  Количество элементов на странице
     * @return Объект PageRequest
     */
    private PageRequest createPageRequest(int from, int size) {
        return PageRequest.of(
                from / size,
                size,
                Sort.by("eventDate").ascending()
        );
    }
}
