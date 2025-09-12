package ru.practicum.ewm.users.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.ewm.events.enumeration.EventState;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.users.dto.ChangeRequestStatusDto;
import ru.practicum.ewm.users.dto.ParticipationRequestDto;
import ru.practicum.ewm.users.dto.UserParticipationRequestDto;
import ru.practicum.ewm.users.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.users.model.ParticipationRequest;
import ru.practicum.ewm.users.model.RequestStatus;
import ru.practicum.ewm.users.repository.RequestRepository;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

/**
 * Реализация сервиса для работы с запросами на участие в событиях.
 * <p>
 * Обрабатывает создание, получение и изменение статуса запросов, а также проверку прав пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден!";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Событие с идентификатором %d не найдено!";
    private static final String EVENT_FULL_MESSAGE = "Событие %d полностью забронировано";

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    private final UserClient userClient;

    /**
     * Получает все заявки пользователя.
     */
    @Override
    public List<ParticipationRequestDto> getAllRequestsByUser(long requesterId) {
        return requestRepository.findByRequesterId(requesterId)
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Создаёт новую заявку на участие в событии.
     */
    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        validateUserExists(userId);
        Event event = getEventOrThrow(eventId);

        validateUserNotInitiator(userId, event);
        validateNoDuplicateRequest(userId, event);
        validateEventAvailability(event);
        validateRequestCapacity(event);

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .event(event)
                .status(calculateInitialRequestStatus(event))
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Создана заявка {} от пользователя {} на событие {}", saved.getId(), userId, eventId);
        return participationRequestMapper.toDto(saved);
    }

    /**
     * Отменяет заявку пользователя.
     */
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(notFoundException("Заявка {0} пользователя {1} не найдена", requestId, userId));

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);
        log.info("Отменена заявка {} пользователя {}", requestId, userId);
        return participationRequestMapper.toDto(updated);
    }

    /**
     * Получает заявки пользователя на конкретное событие.
     */
    @Override
    public List<ParticipationRequestDto> getUserRequestsForEvent(long userId, long eventId) {
        return requestRepository.findByEventIdAndEventInitiatorId(eventId, userId)
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Обновляет статусы заявок на участие.
     */
    @Override
    public UserParticipationRequestDto updateRequestStatus(ChangeRequestStatusDto dto,
                                                           long userId,
                                                           long eventId) {
        Event event = getEventOrThrow(eventId);
        List<ParticipationRequest> requests = validateAndFetchRequests(dto.getRequestIds(), eventId);
        validateRequestCapacity(event);
        processRequests(dto.getStatus(), requests);
        return mapToResponse(requests);
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

    private Event getEventOrThrow(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(notFoundException(EVENT_NOT_FOUND_MESSAGE, eventId));
    }

    private void validateUserNotInitiator(Long userId, Event event) {
        if (userId.equals(event.getInitiatorId())) {
            throw new ConflictException("Инициатор события не может подавать заявки");
        }
    }

    private void validateNoDuplicateRequest(Long userId, Event event) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, event.getId())) {
            throw new ConflictException("Пользователь уже подал заявку на это событие");
        }
    }

    private void validateEventAvailability(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
    }

    private RequestStatus calculateInitialRequestStatus(Event event) {
        return event.isRequestModeration() && event.getParticipantLimit() > 0
                ? RequestStatus.PENDING
                : RequestStatus.CONFIRMED;
    }

    private List<ParticipationRequest> validateAndFetchRequests(List<Long> requestIds, long eventId) {
        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Не все заявки найдены");
        }

        if (!requests.stream().allMatch(r -> r.getEvent().getId().equals(eventId))) {
            throw new ConflictException("Заявки принадлежат разным событиям");
        }

        if (!requests.stream().allMatch(r -> r.getStatus() == RequestStatus.PENDING)) {
            throw new ConflictException("Найдены заявки, отличные от состояния ОЖИДАНИЕ");
        }

        return requests;
    }

    private void validateRequestCapacity(Event event) {
        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            int remainingSlots = event.getParticipantLimit() - (int) confirmedCount;

            if (remainingSlots <= 0) {
                throw new ConflictException(String.format(EVENT_FULL_MESSAGE, event.getId()));
            }
        }
    }

    private void processRequests(RequestStatus targetStatus, List<ParticipationRequest> requests) {
        for (ParticipationRequest request : requests) {
            request.setStatus(targetStatus);
        }
        requestRepository.saveAll(requests);
    }

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

}