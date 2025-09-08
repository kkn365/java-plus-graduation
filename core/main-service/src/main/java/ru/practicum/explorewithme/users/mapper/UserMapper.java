package ru.practicum.explorewithme.users.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(NewUserRequest newUserRequest);
}