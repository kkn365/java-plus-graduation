package ru.practicum.core.user.service;

import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.user.dto.NewUserRequest;
import ru.practicum.core.user.dto.UserDto;

import java.util.List;

/**
 * Интерфейс сервиса для работы с пользователями.
 * <p>
 * Определяет методы для создания, получения и удаления пользователей.
 */
public interface UserService {

    /**
     * Возвращает список пользователей по списку идентификаторов или всех пользователей с пагинацией.
     *
     * @param ids   список идентификаторов пользователей (опционально)
     * @param offset начальная позиция (смещение)
     * @param limit  количество элементов на странице
     * @return список DTO пользователей
     * @throws IllegalArgumentException если параметры пагинации некорректны
     */
    List<UserDto> getUsers(List<Long> ids, int offset, int limit);

    /**
     * Создаёт нового пользователя на основе данных из запроса.
     *
     * @param request данные нового пользователя
     * @return DTO созданного пользователя
     * @throws DataAlreadyExistException если пользователь с таким email уже существует
     */
    UserDto createUser(NewUserRequest request);

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param userId уникальный идентификатор пользователя
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    void deleteUser(Long userId);

    /**
     * Получает краткую информацию о пользователе по его идентификатору.
     *
     * @param userId уникальный идентификатор пользователя
     * @return DTO краткой информации о пользователе
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    UserShortDto getUserById(Long userId);

    /**
     * Получает краткую информацию о нескольких пользователях по их идентификаторам.
     *
     * @param ids список идентификаторов пользователей
     * @return список DTO краткой информации о пользователях
     */
    List<UserShortDto> getUsersByIds(List<Long> ids);
}