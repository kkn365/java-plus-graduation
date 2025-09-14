package ru.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.model.Event;

/**
 * Mapper для преобразования между сущностью Event и её DTO-представлением.
 * <p>
 * Обеспечивает двустороннее преобразование с учётом специфических правил:
 * - игнорирование идентификатора при создании новой сущности
 * - корректное отображение геолокации (широта и долгота)
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class}
)
public interface EventMapper {

    /**
     * Преобразует DTO события в модель события.
     * <p>
     * Игнорирует поля, которые не должны устанавливаться пользователем при создании события:
     * - состояние события (state)
     * - дата публикации (publishedOn)
     * - инициатор (initiator)
     * - идентификатор (id)
     * - дата создания (createdOn)
     * - категория (category)
     * <p>
     * Корректно отображает координаты из DTO в объект Location модели.
     */
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "locationLon", source = "location.lon")
    @Mapping(target = "locationLat", source = "location.lat")
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toModel(NewEventDto newEventDto);

    /**
     * Преобразует модель события в DTO.
     * <p>
     * Извлекает координаты из объекта Location и преобразует инициатора через пользовательский метод.
     * Поля views и confirmedRequests игнорируются, так как они обновляются отдельно.
     */
    @Mappings({
            @Mapping(target = "views", ignore = true),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(source = "locationLat", target = "location.lat"),
            @Mapping(source = "locationLon", target = "location.lon"),
            @Mapping(target = "initiator", ignore = true)
    })
    EventDto toDto(Event event);

    @Mappings({
            @Mapping(target = "id", source = "event.id"),
            @Mapping(target = "views", ignore = true),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(source = "event.locationLat", target = "location.lat"),
            @Mapping(source = "event.locationLon", target = "location.lon"),
            @Mapping(target = "initiator", source = "userShortDto")
    })
    EventDto toDto(Event event, UserShortDto userShortDto);

    /**
     * Преобразует DTO запроса на обновление события администратором в DTO создания события.
     * <p>
     * Используется для унификации логики обновления событий, независимо от источника (администратор или пользователь).
     */
    NewEventDto toNewEventDto(UpdateEventAdminRequest updateEventAdminRequest);

    /**
     * Преобразует DTO запроса на обновление события пользователем в DTO создания события.
     * <p>
     * Используется для унификации логики обновления событий, независимо от источника (администратор или пользователь).
     */
    NewEventDto toNewEventDto(UpdateEventUserRequest updateEventUserRequest);
}