package ru.practicum.explorewithme.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.practicum.explorewithme.categories.mapper.CategoryMapper;
import ru.practicum.explorewithme.events.dto.EventDto;
import ru.practicum.explorewithme.events.dto.EventShortDto;
import ru.practicum.explorewithme.events.dto.NewEventDto;
import ru.practicum.explorewithme.events.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.events.model.Event;
import ru.practicum.explorewithme.users.mapper.UserMapper;

/**
 * Mapper для преобразования между сущностью Event и её DTO-представлением.
 * <p>
 * Обеспечивает двустороннее преобразование с учётом специфических правил:
 * - игнорирование идентификатора при создании новой сущности
 * - корректное отображение геолокации (широта и долгота)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CategoryMapper.class, UserMapper.class})
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
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toModel(NewEventDto newEventDto);

    /**
     * Преобразует модель события в DTO.
     * <p>
     * Возвращает широту и долготу из объекта Location как отдельные поля DTO.
     * Игнорирует поля views и confirmedRequests, так как они должны обновляться отдельно.
     */
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(source = "locationLat", target = "location.lat")
    @Mapping(source = "locationLon", target = "location.lon")
    EventDto toDto(Event event);

    /**
     * Преобразует DTO запроса на обновление события администратором в DTO создания события.
     * <p>
     * Используется для унификации логики обновления событий, независимо от источника (администратор или пользователь).
     *
     * @param updateEventAdminRequest DTO с данными обновления события от администратора
     * @return DTO с данными нового события
     */
    NewEventDto toNewEventDto(UpdateEventAdminRequest updateEventAdminRequest);

    /**
     * Преобразует DTO запроса на обновление события пользователем в DTO создания события.
     * <p>
     * Используется для унификации логики обновления событий, независимо от источника (администратор или пользователь).
     *
     * @param updateEventUserRequest DTO с данными обновления события от пользователя
     * @return DTO с данными нового события
     */
    NewEventDto toNewEventDto(UpdateEventUserRequest updateEventUserRequest);

    /**
     * Преобразует сущность события в её краткое DTO-представление.
     *
     * @param event Сущность события
     * @return Краткое DTO-представление события
     */
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "title", target = "title"),
            @Mapping(source = "annotation", target = "annotation"),
            @Mapping(source = "eventDate", target = "eventDate"),
            @Mapping(source = "paid", target = "paid"),
            @Mapping(source = "category", target = "category"),
            @Mapping(source = "initiator", target = "initiator"),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(target = "views", ignore = true),
    })
    EventShortDto toShortDto(Event event);
}