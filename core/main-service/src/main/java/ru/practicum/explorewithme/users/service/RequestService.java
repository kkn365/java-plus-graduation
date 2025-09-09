package ru.practicum.explorewithme.users.service;

import ru.practicum.explorewithme.users.dto.ChangeRequestStatusDto;
import ru.practicum.explorewithme.users.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.users.dto.UserParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> findAllRequestsByUserId(long userId);

    ParticipationRequestDto save(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long eventId);

    List<ParticipationRequestDto> findUserRequestsOnEvent(long userId, long eventId);

    UserParticipationRequestDto patchRequestStatus(ChangeRequestStatusDto changeRequestStatusDto, long userId, long eventId);

}
