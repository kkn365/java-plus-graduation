package ru.practicum.explorewithme.events.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.hit.HitClient;
import ru.practicum.client.stats.StatsClient;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.explorewithme.categories.model.Category;
import ru.practicum.explorewithme.categories.service.CategoryService;
import ru.practicum.explorewithme.events.dto.AdminEventParams;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.events.dto.UserEventParams;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.events.enumeration.EventStateAction;
import ru.practicum.explorewithme.events.enumeration.EventSortEnum;
import ru.practicum.explorewithme.events.mapper.EventMapper;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.model.EventRequestCount;
import ru.practicum.explorewithme.users.model.RequestStatus;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.RequestRepository;
import ru.practicum.explorewithme.users.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.events.repository.EventRepository.AdminEventSpec.withAdminParams;
import static ru.practicum.explorewithme.events.repository.EventRepository.UserEventSpec.withUserParams;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestRepository requestRepository;
    private final HitClient hitClient;
    private final StatsClient statsClient;

    /**
     * Создаёт новое событие на основе данных из DTO и идентификатора пользователя.
     *
     * @param newEventDto данные нового события
     * @param userId      идентификатор пользователя (инициатор события)
     * @return DTO созданного события
     */
    @Override
    public EventDto addEvent(NewEventDto newEventDto, Long userId) {
        // Получаем сущность пользователя и категории из сервисов
        User user = userService.getUser(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        // Преобразуем DTO в модель события
        Event event = eventMapper.toModel(newEventDto);

        // Устанавливаем значения по умолчанию для необязательных полей
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(Objects.requireNonNullElse(newEventDto.getPaid(), false));
        event.setParticipantLimit(Objects.requireNonNullElse(newEventDto.getParticipantLimit(), 0));
        event.setRequestModeration(Objects.requireNonNullElse(newEventDto.getRequestModeration(), true));

        // Сохраняем событие в репозитории
        Event savedEvent = eventRepository.save(event);

        // Преобразуем сохранённую модель обратно в DTO
        EventDto savedEventDto = eventMapper.toDto(savedEvent);
        log.info("Событие создано: {}", savedEventDto);
        return savedEventDto;
    }

    /**
     * Обновляет событие пользователем-инициатором.
     * <p>
     * Проверяет, что событие принадлежит пользователю и находится в состоянии ОЖИДАНИЕ или ОТМЕНЕНО.
     *
     * @param eventId       Идентификатор события
     * @param newEventDto   Данные для обновления события
     * @param userId        Идентификатор пользователя (инициатор)
     * @return DTO обновлённого события
     */
    @Override
    public EventDto updateEventByUser(Long eventId, UpdateEventUserRequest newEventDto, Long userId) {
        Event event = findEventById(eventId);
        validateInitiator(event, userId);

        if (!List.of(EventState.CANCELED, EventState.PENDING).contains(event.getState())) {
            throw new ConflictException("Можно изменять только события в состоянии ОЖИДАНИЕ или ОТМЕНЕНО");
        }

        NewEventDto updatedData = eventMapper.toNewEventDto(newEventDto);

        // Обновляем данные события
        return updateEvent(event, updatedData);
    }

    /**
     * Обновляет событие администратором.
     * <p>
     * Позволяет изменять состояние события (ОПУБЛИКОВАТЬ, ОТКЛОНИТЬ) и другие параметры.
     * Выполняются проверки на корректность действий и текущего состояния события.
     *
     * @param eventId       Идентификатор события
     * @param newEventDto   DTO с новыми данными события
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

        NewEventDto updatedData = eventMapper.toNewEventDto(newEventDto);

        // Обновляем данные события
        return updateEvent(event, updatedData);
    }

    @Override
    public List<EventDto> findAllByParams(Long userId, Integer from, Integer size) {
        UserDto userDto = userService.getById(userId);
        int page = from / size;

        List<Event> events = eventRepository.findAllByInitiatorId(userDto.getId(),
                PageRequest.of(page, size)).stream().toList();

        List<EventDto> eventDtos = events.stream()
                .map(eventMapper::toDto)
                .toList();


        loadViews(
                eventDtos,
                eventDtos.stream().map(EventDto::getCreatedOn).min(LocalDateTime::compareTo).orElse(LocalDateTime.now()),
                eventDtos.stream().map(EventDto::getEventDate).min(LocalDateTime::compareTo).orElse(null)
        );
        loadConfirmedRequests(eventDtos);

        return eventDtos;
    }

    /**
     * Возвращает событие в виде DTO для указанного пользователя.
     * <p>
     * Метод проверяет, что событие принадлежит пользователю, и загружает дополнительную статистику.
     *
     * @param userId   Идентификатор пользователя (инициатор события)
     * @param eventId  Идентификатор события
     * @return DTO события с полной информацией
     */
    @Override
    public EventDto findUserEvent(Long userId, Long eventId) {
        // Получаем событие по ID
        Event event = findEventById(eventId);

        // Проверяем, что событие принадлежит пользователю
        validateInitiator(event, userId);

        // Преобразуем модель события в DTO
        EventDto eventDto = eventMapper.toDto(event);

        // Загружаем статистику просмотров и количество подтверждённых заявок
        loadViews(List.of(eventDto), event.getPublishedOn(), event.getEventDate());
        loadConfirmedRequests(List.of(eventDto));

        return eventDto;
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
        // Создание объекта PageRequest для пагинации и сортировки
        PageRequest pageRequest = PageRequest.of(
                adminEventParams.getFrom() / adminEventParams.getSize(),
                adminEventParams.getSize(),
                Sort.by("eventDate").ascending()
        );

        // Получение событий из репозитория по спецификации
        Page<Event> eventsPage = eventRepository.findAll(withAdminParams(adminEventParams), pageRequest);

        // Преобразование моделей событий в DTO
        List<EventDto> eventDtos = eventsPage.stream()
                .map(eventMapper::toDto)
                .toList();

        // Подгрузка статистики просмотров и количества подтверждённых заявок
        loadViews(eventDtos, adminEventParams.getRangeStart(), adminEventParams.getRangeEnd());
        loadConfirmedRequests(eventDtos);

        return eventDtos;
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
        if (userEventParams.getRangeStart() != null &&
            userEventParams.getRangeEnd() != null &&
            !userEventParams.getRangeEnd().isAfter(userEventParams.getRangeStart())) {
            throw new ValidationException("rangeEnd должен быть позже, чем rangeStart");
        }

        // Создание объекта PageRequest для пагинации и сортировки
        PageRequest pageRequest = PageRequest.of(
                userEventParams.getFrom() / userEventParams.getSize(),
                userEventParams.getSize(),
                Sort.by("eventDate").ascending()
        );

        // Получение событий из репозитория по спецификации
        Page<Event> eventsPage = eventRepository.findAll(withUserParams(userEventParams), pageRequest);

        // Преобразование моделей событий в DTO
        // ArrayList для сортировки
        List<EventDto> eventDtos = new java.util.ArrayList<>(eventsPage.stream()
                .map(eventMapper::toDto)
                .toList());

        // Загрузка статистики просмотров и подтверждённых заявок
        loadViews(eventDtos, userEventParams.getRangeStart(), userEventParams.getRangeEnd());
        loadConfirmedRequests(eventDtos);

        // Проверяем наличие и значение параметра сортировки
        if (userEventParams.getSort() != null
            && userEventParams.getSort().equals(EventSortEnum.VIEWS)) {
            eventDtos.sort(Comparator.comparing(EventDto::getViews).reversed());
        }

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
        hitClient.hit(dto);
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

        EventDto eventDto = eventMapper.toDto(event);

        loadViews(List.of(eventDto), event.getPublishedOn(), event.getEventDate());
        loadConfirmedRequests(List.of(eventDto));

        return eventDto;
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
        if (!event.getInitiator().getId().equals(userId)) {
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
     * @param action  Действие над состоянием события
     * @param event   Текущее событие
     * @throws ConflictException если действие или состояние события некорректны
     */
    private void validateAdminStateAction(EventStateAction action, Event event) {
        switch (action) {
            case PUBLISH_EVENT:
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Нельзя изменить состояние события, так как оно не находится в правильном состоянии: ОЖИДАНИЕ");
                }
                validatePublishDate(event);
                break;
            case REJECT_EVENT:
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConflictException("Нельзя изменить состояние события, так как оно не находится в правильном состоянии: ОПУБЛИКОВАНО");
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
                        EventRequestCount::getEventId,
                        EventRequestCount::getRequestsCount
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
        Map<String, Long> uriToHits;

        // Создаём маппинг между ID события и URI для запроса статистики
        Map<Long, String> eventUriMap = events.stream()
                .collect(Collectors.toMap(EventDto::getId, event -> "/events/" + event.getId()));

        // Получаем статистику просмотров из внешнего сервиса
        Optional<Collection<HitsStatDTO>> statsResponse = statsClient.getAll(
                start,
                end,
                eventUriMap.values().stream().toList(),
                true
        );

        // Если данные получены, создаём маппинг URI → количество просмотров
        if (statsResponse.isPresent() && !statsResponse.get().isEmpty()) {
            uriToHits = statsResponse.get().stream()
                    .collect(Collectors.toMap(HitsStatDTO::getUri, HitsStatDTO::getHits));
        } else {
            // Если данных нет, устанавливаем просмотры в 0 для всех событий
            events.forEach(event -> event.setViews(0L));
            return;
        }

        // Обновляем DTO событий значениями статистики
        for (EventDto event : events) {
            String uri = eventUriMap.get(event.getId());
            event.setViews(uriToHits.getOrDefault(uri, 0L));
        }
    }

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

        EventDto eventDto = eventMapper.toDto(event);

        loadViews(List.of(eventDto), event.getPublishedOn(), event.getEventDate());
        loadConfirmedRequests(List.of(eventDto));

        return eventDto;
    }
}
