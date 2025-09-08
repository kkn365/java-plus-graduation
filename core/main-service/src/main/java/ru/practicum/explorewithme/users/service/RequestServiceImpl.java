package ru.practicum.explorewithme.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.events.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.users.dto.ChangeRequestStatusDto;
import ru.practicum.explorewithme.users.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.users.dto.UserParticipationRequestDto;
import ru.practicum.explorewithme.users.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.users.model.ParticipationRequest;
import ru.practicum.explorewithme.users.model.RequestStatus;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.RequestRepository;
import ru.practicum.explorewithme.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.explorewithme.exception.NotFoundException.notFoundException;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "Пользователь с идентификатором {0} не найден!";
    private static final String EVENT_NOT_FOUND_EXCEPTION_MESSAGE = "Событие с идентификатором {0} не найдено!";

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> findAllRequestsByUserId(long userId) {
        return ParticipationRequestMapper.mapToDTO(requestRepository.findParticipationRequestByRequester_Id(userId));
    }

    @Override
    public ParticipationRequestDto save(long userId, long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(notFoundException(EVENT_NOT_FOUND_EXCEPTION_MESSAGE, eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(notFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE, userId));

        checkDuplicateRequest(userId, eventId);
        checkUserIsInitiator(event, userId);
        checkParticipantLimitEqualRequestsOnEvent(event);
        checkOnNotPublishedEvent(event);

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .status(event.getRequestModeration() && event.getParticipantLimit() != 0 ?
                        RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        requestRepository.save(participationRequest);

        return ParticipationRequestMapper.mapToDTO(participationRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {

        ParticipationRequest participationRequest = requestRepository.findParticipationRequestByIdAndRequester_Id(requestId, userId)
                .orElseThrow(notFoundException("Запрос {0} на участие пользователя {1} не найден!", requestId, userId));
        participationRequest.setStatus(RequestStatus.CANCELED);
        requestRepository.save(participationRequest);

        return ParticipationRequestMapper.mapToDTO(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> findUserRequestsOnEvent(long userId, long eventId) {
        return ParticipationRequestMapper.mapToDTO(
                requestRepository.findParticipationRequestByEvent_IdAndEvent_Initiator_Id(eventId, userId));
    }

    @Override
    public UserParticipationRequestDto patchRequestStatus(ChangeRequestStatusDto changeRequestStatusDto, long userId, long eventId) {

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        List<ParticipationRequest> requests = requestRepository.findAllById(changeRequestStatusDto.getRequestIds());
        Event event = eventRepository.findById(eventId)
                .orElseThrow(notFoundException(EVENT_NOT_FOUND_EXCEPTION_MESSAGE, eventId));

        long allRequestsOnEventCount = requestRepository.countParticipationRequestByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();
        long canConfirmRequestsNumber = participantLimit == 0 ? requests.size() : participantLimit - allRequestsOnEventCount;

        if (canConfirmRequestsNumber <= 0) {
            throw new ConflictException(String.format(
                    "Событие с идентификатором: %d недоступно для добавления новых заявок.", eventId
            ));
        }

        for (ParticipationRequest participationRequest : requests) {
            // статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
            if (participationRequest.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Status not pending: request id:  " + participationRequest.getId());
            }
        }

        for (ParticipationRequest request : requests) {
            if (changeRequestStatusDto.getStatus() == RequestStatus.REJECTED || canConfirmRequestsNumber <= 0) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
                continue;
            }

            request.setStatus(RequestStatus.CONFIRMED);
            confirmedRequests.add(request);
            canConfirmRequestsNumber--;
        }

        requestRepository.saveAll(requests);

        return UserParticipationRequestDto.builder()
                .confirmedRequests(ParticipationRequestMapper.mapToDTO(confirmedRequests))
                .rejectedRequests(ParticipationRequestMapper.mapToDTO(rejectedRequests))
                .build();
    }

    private void checkOnNotPublishedEvent(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }
    }

    private void checkDuplicateRequest(long userId, long eventId) {
        if (requestRepository.countParticipationRequestByRequesterIdAndEvent_Id(userId, eventId) != 0) {
            throw new ConflictException("Duplicate request");
        }
    }

    private void checkUserIsInitiator(Event event, long userId) {
        if (event.getInitiator().getId() == userId) {
            throw new ConflictException("User is Initiator of Request Exception");
        }
    }

    private void checkParticipantLimitEqualRequestsOnEvent(Event event) {
        if (event.getParticipantLimit() == requestRepository.countParticipationRequestByEvent_IdAndStatus(event.getId(), RequestStatus.CONFIRMED)
                && event.getParticipantLimit() != 0) {
            throw new ConflictException("Participant limit is equal to request limit");
        }
    }
}