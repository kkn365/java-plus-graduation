package ru.practicum.explorewithme.users.mapper;

import ru.practicum.explorewithme.users.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.users.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ParticipationRequestMapper {

    public static ParticipationRequestDto mapToDTO(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .requester(participationRequest.getRequester().getId())
                .event(participationRequest.getEvent().getId())
                .created(participationRequest.getCreated())
                .status(participationRequest.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> mapToDTO(List<ParticipationRequest> participationRequestList) {
        return participationRequestList.stream()
                .map(ParticipationRequestMapper::mapToDTO)
                .collect(Collectors.toList());
    }
}
