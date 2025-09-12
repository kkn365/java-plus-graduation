package ru.practicum.core.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.user.dto.NewUserRequest;
import ru.practicum.core.user.dto.UserDto;
import ru.practicum.core.user.mapper.UserMapper;
import ru.practicum.core.user.model.User;
import ru.practicum.core.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String EMAIL_ALREADY_EXISTS = "Пользователь с email %s уже существует";
    private static final String USER_NOT_FOUND = "Пользователь с ID %d не найден";
    private static final String INVALID_PAGINATION_PARAMS = "Параметры from и size должны быть положительными числами";

    /**
     * Создаёт нового пользователя на основе данных из запроса.
     * <p>
     * Выполняет проверку уникальности email перед сохранением.
     * Логирует создание пользователя и возвращает DTO.
     *
     * @param request данные нового пользователя
     * @return DTO созданного пользователя
     * @throws DataAlreadyExistException если пользователь с таким email уже существует
     */
    @Override
    public UserDto createUser(NewUserRequest request) {
        String userEmail = request.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            throw new DataAlreadyExistException(String.format(EMAIL_ALREADY_EXISTS, userEmail));
        }

        User user = userMapper.toModel(request);
        User savedUser = userRepository.save(user);
        log.info("Создан пользователь: {} (email: {})", savedUser.getName(), userEmail);
        return userMapper.toDto(savedUser);
    }

    /**
     * Возвращает список пользователей по списку идентификаторов или всех пользователей с пагинацией.
     * <p>
     * Если список идентификаторов не пустой, возвращаются только указанные пользователи.
     * Если список пустой или null, возвращаются все пользователи с учётом параметров пагинации.
     *
     * @param ids   список идентификаторов пользователей
     * @param from  начальная позиция (смещение)
     * @param size  количество элементов на странице
     * @return список DTO пользователей
     * @throws IllegalArgumentException если параметры пагинации некорректны
     */
    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException(INVALID_PAGINATION_PARAMS);
        }

        if (ids != null && !ids.isEmpty()) {
            log.info("Запрос пользователей по идентификаторам: {}", ids);
            return userRepository.findAllById(ids).stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        }

        int pageNumber = Math.floorDiv(from, size);
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<User> userPage = userRepository.findAll(pageable);

        log.info("Запрошена страница {} с размером {}", pageNumber, size);
        return userPage.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * <p>
     * Выполняет проверку существования пользователя перед удалением.
     * Логирует успешное удаление или ошибки при выполнении операции.
     *
     * @param userId уникальный идентификатор пользователя
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    @Override
    public void deleteUser(Long userId) {
        User user = getUser(userId);
        try {
            log.info("Начало удаления пользователя: {} (ID: {})", user.getName(), userId);
            userRepository.deleteById(userId);
            log.info("Пользователь успешно удалён: {}", user.getName());
        } catch (Exception e) {
            log.error("Ошибка удаления пользователя {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }

    /**
     * Получает краткую информацию о пользователе по его идентификатору.
     *
     * @param userId уникальный идентификатор пользователя
     * @return DTO краткой информации о пользователе
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    @Override
    public UserShortDto getUserById(Long userId) {
        User user = getUser(userId);
        log.info("Получен пользователь: {} (ID: {})", user.getName(), userId);
        return userMapper.toShortDto(user);
    }

    /**
     * Получает краткую информацию о нескольких пользователях по их идентификаторам.
     *
     * @param ids список идентификаторов пользователей
     * @return список DTO краткой информации о пользователях
     */
    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toShortDto)
                .toList();
    }

    /**
     * Возвращает сущность пользователя по его идентификатору.
     * <p>
     * Если пользователь не найден, выбрасывается исключение {@link NotFoundException}.
     *
     * @param userId уникальный идентификатор пользователя
     * @return сущность пользователя
     * @throws NotFoundException если пользователь с указанным идентификатором не существует
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(notFoundException(USER_NOT_FOUND, userId));
    }
}