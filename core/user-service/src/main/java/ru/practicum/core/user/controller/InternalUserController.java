package ru.practicum.core.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Internal: Пользователи", description = "Операции для получения информации о пользователях (внутренний доступ)")
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
    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает краткую информацию о пользователе по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                    content = @Content(schema = @Schema(implementation = UserShortDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
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
    @Operation(summary = "Получить список пользователей",
            description = "Возвращает краткую информацию о нескольких пользователях по их идентификаторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "404", description = "Один или несколько пользователей не найдены"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Override
    public ResponseEntity<List<UserShortDto>> getUsers(List<Long> ids) throws FeignException {
        log.info("GET /internal/user - Получен внутренний запрос на получение информации о пользователях");
        return ResponseEntity.ok().body(userService.getUsersByIds(ids));
    }
}