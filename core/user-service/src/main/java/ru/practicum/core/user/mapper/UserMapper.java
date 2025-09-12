package ru.practicum.core.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.user.model.User;
import ru.practicum.core.user.dto.NewUserRequest;
import ru.practicum.core.user.dto.UserDto;

/**
 * Mapper для преобразования между моделью User и DTO.
 * <p>
 * Использует MapStruct для автоматической генерации кода маппинга.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Преобразует сущность пользователя в DTO.
     *
     * @param user пользователь
     * @return DTO пользователя
     */
    UserDto toDto(User user);

    /**
     * Преобразует DTO нового пользователя в сущность пользователя.
     * <p>
     * Поле id игнорируется, так как оно генерируется при сохранении.
     *
     * @param newUserRequest данные нового пользователя
     * @return сущность пользователя
     */
    @Mapping(target = "id", ignore = true)
    User toModel(NewUserRequest newUserRequest);

    /**
     * Метод преобразует сущность {@link User} в DTO {@link UserShortDto}.
     * <p>
     * Используется для упрощения передачи краткой информации о пользователе между микросервисами.
     *
     * @param user пользователь, который необходимо преобразовать
     * @return объект типа {@link UserShortDto} с краткой информацией о пользователе
     */
    UserShortDto toShortDto(User user);
}