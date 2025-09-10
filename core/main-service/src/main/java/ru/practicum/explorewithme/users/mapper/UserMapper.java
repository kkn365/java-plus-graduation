package ru.practicum.explorewithme.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.model.User;

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
}