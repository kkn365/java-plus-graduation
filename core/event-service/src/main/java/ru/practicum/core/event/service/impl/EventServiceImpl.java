package ru.practicum.core.event.service.impl;

import com.google.protobuf.Timestamp;
import feign.FeignException;
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
import ru.practicum.core.event.model.enums.events.EventSort;
import ru.practicum.core.event.model.enums.events.EventStateAction;
import ru.practicum.core.event.repository.EventRepository;
import ru.practicum.core.event.service.api.CategoryService;
import ru.practicum.core.event.service.api.EventService;
import ru.practicum.recomm.client.AnalyzerClient;
import ru.practicum.recomm.client.CollectorClient;
import ru.practicum.recommendations.messages.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.core.event.model.enums.events.EventSort.EVENT_DATE;
import static ru.practicum.core.event.repository.EventRepository.AdminEventSpec.withAdminParams;
import static ru.practicum.core.event.repository.EventRepository.UserEventSpec.withUserParams;

/**
 * Реализация сервиса для работы с событиями.
 * <p>
 * Класс содержит бизнес-логику для управления событиями, включая создание, обновление, удаление и получение информации о событиях.
 * Использует репозиторий для взаимодействия с базой данных и клиенты для обращения к другим микросервисам.
 */
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
    private static final String RANGE_ERROR_MESSAGE = "Некорректный диапазон";

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;

    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;
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
     * @param eventId     идентификатор события, которое требуется обновить
     * @param newEventDto DTO с данными, которые необходимо изменить
     * @param userId      идентификатор пользователя, инициирующего обновление события
     * @return DTO обновлённого события
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
     * @param eventId     идентификатор события, которое необходимо обновить
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
     * @throws NotFoundException   если пользователь не найден
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
     * @param userId  идентификатор пользователя, который является инициатором события
     * @param eventId идентификатор события, информацию о котором требуется получить
     * @return DTO события с дополнительной информацией
     * @throws NotFoundException если событие или пользователь не найдены
     * @throws ConflictException если событие не принадлежит указанному пользователю
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
                && userEventParams.getSort().equals(EventSort.RATING)) {
            log.debug("Применена сортировка по рейтингу");
            eventDtos.sort(Comparator.comparing(EventDto::getRating).reversed());
        }

        // Возвращаем список DTO событий
        return eventDtos;
    }

    /**
     * Возвращает информацию о событии, доступную публично.
     * <p>
     * Метод проверяет, существует ли событие с указанным идентификатором, и находится ли оно в состоянии "Опубликовано".
     * Если событие опубликовано, проверяется существование пользователя с указанным ID.
     * Затем отправляется действие "просмотр" в коллектор для дальнейшей обработки,
     * и формируется объект DTO события с дополнительной информацией.
     *
     * @param eventId уникальный идентификатор события, которое запрашивается
     * @param userId  идентификатор пользователя, который просматривает событие (может быть null)
     * @return объект {@link EventDto}, содержащий информацию о событии
     * @throws NotFoundException если событие не найдено, неопубликовано или пользователь не найден
     */
    @Override
    public EventDto findPublishedEvent(Long eventId, Long userId) {
        Event event = findEventById(eventId);

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException(String.format(EVENT_NOT_PUBLISHED_ERROR_MESSAGE, eventId));
        }
        // Проверяем, что пользователь с указанным ID существует
        getUserOrThrow(userId);
        // Отправляем действие пользователя в коллектор
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    /**
     * Метод получения DTO события по его идентификатору.
     * <p>
     * Используется микросервисами.
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
     * Используется микросервисами.
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
     * Возвращает список рекомендованных событий для указанного пользователя.
     * <p>
     * Метод проверяет существование пользователя с указанным идентификатором, формирует запрос к сервису аналитики,
     * получает список рекомендаций и преобразует его в DTO-объекты событий. Рекомендации сортируются по убыванию релевантности.
     *
     * @param userId уникальный идентификатор пользователя, для которого формируются рекомендации
     * @return отсортированный список объектов {@link EventDto}, содержащих информацию о рекомендуемых событиях
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    @Override
    public List<EventDto> getRecommendations(Long userId) {
        // Проверяем, что пользователь с указанным ID существует
        getUserOrThrow(userId);
        // Формируем запрос к сервису аналитики
        UserPredictionsRequest userPredictionsRequest = UserPredictionsRequest.newBuilder()
                .setUserId(userId)
                .build();
        // Получаем рекомендации для пользователя
        List<RecommendedEvent> recommendedEvents = analyzerClient.getRecommendationsForUser(userPredictionsRequest);
        // Если рекомендаций нет, возвращаем пустой список
        if (recommendedEvents.isEmpty()) {
            return List.of();
        }
        // Преобразовываем рекомендации в Map<Long, Double>
        Map<Long, Double> recommendations = recommendedEvents.stream()
                .collect(Collectors.toMap(
                        RecommendedEvent::getEventId,
                        RecommendedEvent::getScore
                ));
        // Получаем события по ID
        List<Event> events = eventRepository.findAllById(recommendations.keySet());
        // Создаем DTO событий с дополнительной информацией
        List<EventDto> eventDtos = createEventDtoListWithAdditionalInfo(events);
        // Сортируем DTO по релевантности и возвращаем результат
        return eventDtos.stream()
                .sorted((e1, e2) -> {
                    double s1 = recommendations.getOrDefault(e1.getId(), 0.0);
                    double s2 = recommendations.getOrDefault(e2.getId(), 0.0);
                    return Double.compare(s2, s1); // По убыванию
                })
                .toList();
    }

    /**
     * Добавляет лайк к событию от пользователя.
     * <p>
     * Метод проверяет существование пользователя и события, а также состояние события. Если событие опубликовано,
     * отправляется действие "лайк" в коллектор для дальнейшей обработки.
     *
     * @param eventId   уникальный идентификатор события, которому добавляется лайк
     * @param userId    уникальный идентификатор пользователя, который ставит лайк
     * @throws NotFoundException если пользователь или событие не найдены, либо событие не находится в состоянии "Опубликовано"
     */
    @Override
    public void addLike(Long eventId, Long userId) {
        // Проверяем, что пользователь с указанным ID существует
        getUserOrThrow(userId);
        // Проверяем, что событие с указанным ID существует
        Event event = findEventById(eventId);
        // Проверяем, что событие находится в состоянии "Опубликовано"
        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException(String.format(EVENT_NOT_PUBLISHED_ERROR_MESSAGE, eventId));
        }
        // Отправляем действие пользователя в коллектор
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    /**
     * Отправляет действие пользователя в коллектор.
     * <p>
     * Создаёт объект {@link UserActionProto} с текущим временем в формате UTC и передаёт его в сервис collectorClient.
     *
     * @param userId     идентификатор пользователя
     * @param eventId    идентификатор события
     * @param actionType тип действия (VIEW, LIKE и т.д.)
     */
    private void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        long epochSecond = now.atOffset(ZoneOffset.UTC).toEpochSecond();

        UserActionProto userAction = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder().setSeconds(epochSecond).build())
                .build();

        collectorClient.newUserAction(userAction);
        log.debug("В коллектор отправлено действие пользователя c ID={} с типом {} на событие c ID={}",
                userId, actionType, eventId);
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
     * Создаёт список объектов DTO событий с дополнительной информацией.
     * <p>
     * Метод собирает информацию о пользователях-инициаторах, количестве подтверждённых заявок и рейтингах событий,
     * и добавляет её в DTO каждого события.
     *
     * @param events список событий, для которых требуется создать DTO
     * @return список объектов {@link EventDto}, содержащих основную информацию об событиях
     *         и дополнительные данные (инициатор, количество подтверждённых заявок, рейтинг)
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

        // Получаем список уникальных ID событий
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // Запрашиваем количество подтверждённых заявок
        Map<Long, Long> confirmedRequestsCount = loadConfirmedRequestsCount(eventsIds);
        // Запрашиваем рейтинги событий
        Map<Long, Double> ratings = loadRatings(eventsIds);

        // Возвращаем список DTO событий c с дополнительной информацией
        return events.stream()
                .map(event -> {
                    EventDto dto = eventMapper.toDto(event);
                    dto.setInitiator(users.get(event.getInitiatorId()));
                    dto.setConfirmedRequests(confirmedRequestsCount.getOrDefault(event.getId(), 0L));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    return dto;
                })
                .toList();
    }

    /**
     * Загружает рейтинги событий на основе их идентификаторов.
     * <p>
     * Метод отправляет запрос в сервис {@code analyzerClient} для получения рейтинга каждого события.
     * Рейтинг формируется на основе пользовательских взаимодействий (например, просмотров, лайков и т.д.).
     *
     * @param eventIds список уникальных идентификаторов событий, для которых нужно получить рейтинг
     * @return карта, где ключ — идентификатор события, значение — его рейтинг
     */
    private Map<Long, Double> loadRatings(List<Long> eventIds) {
        log.debug("Загрузка рейтингов для событий: {}", eventIds);
        // Создание запроса на получение рейтинга
        InteractionsCountRequest request = InteractionsCountRequest.newBuilder()
                .addAllEventIds(eventIds)
                .build();

        // Получение данных из сервиса analyzerClient
        List<RecommendedEvent> recommendedEvents = analyzerClient.getInteractionsCount(request);

        // Преобразование списка в Map<Long, Double>
        return recommendedEvents.stream()
                .collect(Collectors.toMap(
                        RecommendedEvent::getEventId,
                        RecommendedEvent::getScore
                ));
    }

    /**
     * Загружает количество подтверждённых заявок для указанных событий.
     * <p>
     * Метод отправляет запрос в сервис {@code requestClient} и получает список объектов {@link EventRequestsCountDto},
     * которые содержат информацию о количестве подтверждённых заявок на каждое событие.
     * Результат преобразуется в карту, где ключ — идентификатор события, значение — количество подтверждённых заявок.
     *
     * @param eventIds список уникальных идентификаторов событий, для которых нужно получить количество подтверждённых заявок
     * @return карта, где ключ — идентификатор события, значение — количество подтверждённых заявок
     */
    private Map<Long, Long> loadConfirmedRequestsCount(List<Long> eventIds) {
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
            return response.getBody().stream()
                    .collect(Collectors.toMap(
                            EventRequestsCountDto::getEventId,
                            EventRequestsCountDto::getConfirmedRequests
                    ));

        } catch (FeignException fe) {
            log.error("Ошибка при получении количества подтверждённых заявок для событий {}: {}",
                    eventIds, fe.getMessage(), fe);
            return Collections.emptyMap();
        }
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
     * @param event  событие, состояние которого необходимо изменить
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
     */
    private PageRequest createPageRequest(int from, int size) {
        return PageRequest.of(
                from / size,
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
