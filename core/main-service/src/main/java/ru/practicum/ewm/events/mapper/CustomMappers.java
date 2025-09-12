package ru.practicum.ewm.events.mapper;

import org.mapstruct.Named;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.util.Map;
import java.util.Optional;

public class CustomMappers {

    /**
     * Вспомогательный метод для маппинга инициатора события из карты пользователей.
     *
     * @param userId  ID пользователя-инициатора
     * @param userMap Карта (ID пользователя → краткая информация о пользователе)
     * @return Краткая информация о пользователе-инициаторе
     * @throws NotFoundException если пользователь не найден в карте
     */
    @Named("mapUserFromId")
    public static UserShortDto mapUserFromId(Long userId, Map<Long, UserShortDto> userMap) {
        return Optional.ofNullable(userMap.get(userId))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID %d не найден в карте", userId)));
    }
}