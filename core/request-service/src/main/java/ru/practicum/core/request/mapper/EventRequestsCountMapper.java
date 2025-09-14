package ru.practicum.core.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.request.dto.EventRequestsCount;

/**
 * Mapper для преобразования объектов EventRequestsCount в DTO-объекты EventRequestsCountDto.
 * <p>
 * Используется MapStruct для автоматической генерации кода на основе аннотаций.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventRequestsCountMapper {
    /**
     * Преобразует сущность EventRequestsCount в DTO-объект EventRequestsCountDto.
     *
     * @param requestCount объект, содержащий данные о количестве подтверждённых заявок на событие
     * @return DTO-объект с информацией о событии и количестве участников
     */
    EventRequestsCountDto toDto(EventRequestsCount requestCount);
}