package ru.practicum.core.request.service;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.event.client.EventClient;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.api.util.enums.RequestStatus;
import ru.practicum.core.request.dto.ChangeRequestStatusDto;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.dto.UserParticipationRequestDto;
import ru.practicum.core.request.mapper.EventRequestsCountMapper;
import ru.practicum.core.request.mapper.ParticipationRequestMapper;
import ru.practicum.core.request.model.ParticipationRequest;
import ru.practicum.core.request.repository.RequestRepository;
import ru.practicum.recomm.client.CollectorClient;
import ru.practicum.recommendations.messages.ActionTypeProto;
import ru.practicum.recommendations.messages.UserActionProto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

/**
 * Реализация сервиса для работы с запросами на участие в событиях.
 * <p>
 * Обрабатывает создание, получение и изменение статуса запросов, а также проверку прав пользователя.
 * Взаимодействует с внутренними микросервисами (пользовательский и событийный) для валидации данных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден!";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Событие с идентификатором %d не найдено!";
    private static final String EVENT_FULL_MESSAGE = "Событие %d полностью забронировано";
    private static final String RETURNED_STATUS_MESSAGE = "Пользовательский сервис вернул статус {}: {}";

    private final RequestRepository requestRepository;
    private final ParticipationRequestMapper participationRequestMapper;
    private final EventRequestsCountMapper eventRequestsCountMapper;

    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;

    /**
     * Возвращает список всех запросов текущего пользователя.
     *
     * @param requesterId Идентификатор пользователя
     * @return Список DTO запросов
     */
    @Override
    public List<ParticipationRequestDto> getAllRequestsByUser(Long requesterId) {
        return requestRepository.findByRequesterId(requesterId)
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Создаёт новую заявку на участие в событии от имени пользователя.
     * <p>
     * Метод выполняет следующие проверки:
     * - Существует ли пользователь с указанным идентификатором.
     * - Существует ли событие, доступно ли оно для участия и опубликовано ли оно.
     * - Не является ли пользователь инициатором события.
     * - Не отправлял ли пользователь ранее заявку на это событие.
     * - Не превышена ли максимальная вместимость события (если ограничена).
     * <p>
     * После успешных проверок создаётся заявка с начальным статусом и сохраняется в репозитории.
     * Также отправляется действие пользователя в коллектор.
     *
     * @param userId   уникальный идентификатор пользователя, который подаёт заявку
     * @param eventId  уникальный идентификатор события, на которое подаётся заявка
     * @return объект {@link ParticipationRequestDto}, представляющий созданную заявку
     * @throws NotFoundException если пользователь или событие не найдены
     */
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        validateUserExists(userId);
        EventDto eventDto = getPublishedEventOrThrow(eventId);

        validateUserNotInitiator(userId, eventDto);
        validateNoDuplicateRequest(userId, eventDto);
        validateEventAvailability(eventDto);
        validateRequestCapacity(eventDto);

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(calculateInitialRequestStatus(eventDto))
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Создана заявка {} от пользователя {} на событие {}", saved.getId(), userId, eventId);
        // Отправляем действие пользователя в коллектор
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
        return participationRequestMapper.toDto(saved);
    }

    /**
     * Отменяет существующий запрос на участие.
     *
     * @param userId    Идентификатор пользователя
     * @param requestId Идентификатор запроса
     * @return DTO обновлённого запроса
     * @throws NotFoundException Если запрос не найден
     */
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(notFoundException("Заявка {0} пользователя {1} не найдена", requestId, userId));

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);
        log.info("Отменена заявка {} пользователя {}", requestId, userId);
        return participationRequestMapper.toDto(updated);
    }

    /**
     * Получает список заявок на конкретное событие, принадлежащее пользователю.
     *
     * @param userId  Идентификатор инициатора события
     * @param eventId Идентификатор события
     * @return Список DTO запросов
     */
    @Override
    public List<ParticipationRequestDto> getUserRequestsForEvent(Long userId, Long eventId) {
        validateUserExists(userId);
        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Изменяет статус нескольких запросов (подтверждение/отказ).
     * <p>
     * Проверяет корректность выбранных запросов и обновляет их статус.
     *
     * @param dto     DTO с параметрами изменения статуса
     * @param userId  Идентификатор инициатора события
     * @param eventId Идентификатор события
     * @return DTO с результатами обработки
     */
    @Override
    public UserParticipationRequestDto updateRequestStatus(ChangeRequestStatusDto dto,
                                                           Long userId,
                                                           Long eventId) {
        EventDto eventDto = getPublishedEventOrThrow(eventId);
        List<ParticipationRequest> requests = validateAndFetchRequests(dto.getRequestIds(), eventId);
        validateRequestCapacity(eventDto);
        processRequests(dto.getStatus(), requests);
        return mapToResponse(requests);
    }

    /**
     * Возвращает количество подтверждённых запросов для списка событий.
     *
     * @param eventIds Список идентификаторов событий
     * @return Список DTO с количеством запросов
     */
    @Override
    public List<EventRequestsCountDto> getEventRequestsCount(List<Long> eventIds) {
        return requestRepository.countByStatusForEvents(eventIds, RequestStatus.CONFIRMED).stream()
                .map(eventRequestsCountMapper::toDto)
                .toList();
    }

    /**
     * Проверяет, существует ли запрос от пользователя на событие.
     *
     * @param userId  Идентификатор пользователя
     * @param eventId Идентификатор события
     * @return true, если запрос существует
     */
    @Override
    public Boolean hasRequest(Long userId, Long eventId) {
        return requestRepository.existsByRequesterIdAndEventId(userId, eventId);
    }

    /**
     * Проверяет существование пользователя через внешний сервис.
     *
     * @param userId Идентификатор пользователя
     * @throws NotFoundException Если пользователь не найден
     */
    private void validateUserExists(Long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка успешного статуса ответа и наличия тела
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn(RETURNED_STATUS_MESSAGE, response.getStatusCode(), response.getHeaders());
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
     * Получает информацию о событии и проверяет его статус.
     *
     * @param eventId Идентификатор события
     * @return DTO события
     * @throws NotFoundException Если событие не найдено
     */
    private EventDto getPublishedEventOrThrow(Long eventId) {
        try {
            ResponseEntity<EventDto> response = eventClient.getEventById(eventId);

            // Проверка успешного статуса ответа и наличия тела
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn(RETURNED_STATUS_MESSAGE, response.getStatusCode(), response.getHeaders());
                throw new NotFoundException(String.format(EVENT_NOT_FOUND_MESSAGE, eventId));
            }

            if (response.getBody() == null) {
                log.warn("Ответ от пользовательского сервиса не содержит тело для события ID={}", eventId);
                throw new NotFoundException(String.format(EVENT_NOT_FOUND_MESSAGE, eventId));
            }

            return response.getBody();

        } catch (FeignException fe) {
            log.error("Ошибка при проверке существования события ID={}: HTTP {} - {}",
                    eventId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(EVENT_NOT_FOUND_MESSAGE, eventId), fe);
        }
    }

    /**
     * Проверяет, что пользователь не является инициатором события.
     *
     * @param userId        Идентификатор пользователя
     * @param eventShortDto DTO события
     * @throws ConflictException Если пользователь — инициатор события
     */
    private void validateUserNotInitiator(Long userId, EventDto eventShortDto) {
        if (userId.equals(eventShortDto.getInitiator().getId())) {
            throw new ConflictException("Инициатор события не может подавать заявки");
        }
    }

    /**
     * Проверяет, не был ли пользователь уже зарегистрирован на событие.
     *
     * @param userId        Идентификатор пользователя
     * @param eventShortDto DTO события
     * @throws ConflictException Если заявка уже существует
     */
    private void validateNoDuplicateRequest(Long userId, EventDto eventShortDto) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventShortDto.getId())) {
            throw new ConflictException("Пользователь уже подал заявку на это событие");
        }
    }

    /**
     * Проверяет, что событие опубликовано и доступно для регистрации.
     *
     * @param eventDto DTO события
     * @throws ConflictException Если событие не опубликовано
     */
    private void validateEventAvailability(EventDto eventDto) {
        if (!EventState.PUBLISHED.equals(eventDto.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
    }

    /**
     * Определяет начальный статус запроса в зависимости от настроек события.
     *
     * @param eventDto DTO события
     * @return Статус запроса (ОЖИДАНИЕ или ПОДТВЕРЖДЁН)
     */
    private RequestStatus calculateInitialRequestStatus(EventDto eventDto) {
        return eventDto.getRequestModeration() && eventDto.getParticipantLimit() > 0
                ? RequestStatus.PENDING
                : RequestStatus.CONFIRMED;
    }

    /**
     * Проверяет и загружает список запросов.
     * <p>
     * Убеждается, что все запросы существуют, относятся к одному событию и имеют статус ОЖИДАНИЕ.
     *
     * @param requestIds Идентификаторы запросов
     * @param eventId    Идентификатор события
     * @return Список найденных запросов
     * @throws NotFoundException Если не все запросы найдены
     * @throws ConflictException Если запросы не соответствуют требованиям
     */
    private List<ParticipationRequest> validateAndFetchRequests(List<Long> requestIds, Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Не все заявки найдены");
        }

        if (!requests.stream().allMatch(r -> r.getEventId().equals(eventId))) {
            throw new ConflictException("Заявки принадлежат разным событиям");
        }

        if (!requests.stream().allMatch(r -> r.getStatus() == RequestStatus.PENDING)) {
            throw new ConflictException("Найдены заявки, отличные от состояния ОЖИДАНИЕ");
        }

        return requests;
    }

    /**
     * Проверяет, не превышено ли количество участников события.
     *
     * @param eventDto DTO события
     * @throws ConflictException Если событие полностью забронировано
     */
    private void validateRequestCapacity(EventDto eventDto) {
        if (eventDto.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventDto.getId(), RequestStatus.CONFIRMED);
            int remainingSlots = eventDto.getParticipantLimit() - (int) confirmedCount;

            if (remainingSlots <= 0) {
                throw new ConflictException(String.format(EVENT_FULL_MESSAGE, eventDto.getId()));
            }
        }
    }

    /**
     * Обновляет статус всех выбранных запросов.
     *
     * @param targetStatus Новый статус
     * @param requests     Список запросов
     */
    private void processRequests(RequestStatus targetStatus, List<ParticipationRequest> requests) {
        for (ParticipationRequest request : requests) {
            request.setStatus(targetStatus);
        }
        requestRepository.saveAll(requests);
    }

    /**
     * Формирует ответ с результатами обработки запросов.
     *
     * @param requests Список обработанных запросов
     * @return DTO с разделением по статусам
     */
    private UserParticipationRequestDto mapToResponse(List<ParticipationRequest> requests) {
        return UserParticipationRequestDto.builder()
                .confirmedRequests(requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                        .map(participationRequestMapper::toDto)
                        .collect(Collectors.toList()))
                .rejectedRequests(requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                        .map(participationRequestMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
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

}