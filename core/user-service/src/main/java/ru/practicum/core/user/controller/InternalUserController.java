package ru.practicum.core.user.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.user.service.UserService;

import java.util.List;

/**
 * Контроллер для внутреннего API пользователей.
 * <p>
 * Обрабатывает запросы на получение информации о пользователях из других микросервисов.
 */
@Slf4j
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalUserController implements UserClient {

    private final UserService userService;

    /**
     * Получает информацию о пользователе по его идентификатору.
     *
     * @param userId Идентификатор пользователя
     * @return Ответ с краткой информацией о пользователе (200 OK)
     *         или 404 Not Found, если пользователь не найден
     */
    @Override
    public ResponseEntity<UserShortDto> getUser(Long userId) throws FeignException {
        log.info("GET /internal/user/{} - Получен внутренний запрос на получение информации о пользователе", userId);
        return ResponseEntity.ok().body(userService.getUserById(userId));
    }

    /**
     * Получает информацию о нескольких пользователях по их идентификаторам.
     *
     * @param ids Список идентификаторов пользователей
     * @return Ответ со списком краткой информации о пользователях (200 OK)
     *         или 404 Not Found, если хотя бы один пользователь не найден
     */
    @Override
    public ResponseEntity<List<UserShortDto>> getUsers(List<Long> ids) throws FeignException {
        log.info("GET /internal/user - Получен внутренний запрос на получение информации о пользователях");
        return ResponseEntity.ok().body(userService.getUsersByIds(ids));
    }
}
