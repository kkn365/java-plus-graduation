package ru.practicum.explorewithme.users.service;

import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.exception.DataAlreadyExistException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.users.model.User;

import java.util.List;

/**
 * Интерфейс сервиса для работы с пользователями.
 * <p>
 * Определяет методы для создания, получения и удаления пользователей.
 */
public interface UserService {
    /**
     * Создаёт нового пользователя на основе данных из запроса.
     *
     * @param request данные нового пользователя
     * @return DTO созданного пользователя
     * @throws DataAlreadyExistException если пользователь с таким email уже существует
     */
    UserDto createUser(NewUserRequest request);

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
     * Удаляет пользователя по его идентификатору.
     *
     * @param userId уникальный идентификатор пользователя
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    void deleteUser(Long userId);

    /**
     * Возвращает пользователя по его идентификатору в виде DTO.
     *
     * @param userId уникальный идентификатор пользователя
     * @return DTO пользователя
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    UserDto getById(Long userId);

    /**
     * Возвращает сущность пользователя по его идентификатору.
     *
     * @param userId уникальный идентификатор пользователя
     * @return сущность пользователя
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    User getUser(Long userId);
}