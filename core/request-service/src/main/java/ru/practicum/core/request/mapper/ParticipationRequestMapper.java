package ru.practicum.core.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.model.ParticipationRequest;

/**
 * Mapper для преобразования между моделью ParticipationRequest и DTO.
 * <p>
 * Использует MapStruct для автоматической генерации кода маппинга.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParticipationRequestMapper {

    /**
     * Преобразует сущность запроса на участие в событие в DTO.
     *
     * @param participationRequest модель запроса
     * @return DTO запроса
     */
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "event", source = "eventId")
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);
}