package ru.practicum.core.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.user.dto.NewUserRequest;
import ru.practicum.core.user.dto.UserDto;
import ru.practicum.core.user.service.UserService;

import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.*;

/**
 * Контроллер для работы с внешними запросами, связанными с пользователями.
 * <p>
 * Предоставляет REST-эндпоинты для управления пользователями, доступными только для ролей с правами администратора.
 * Взаимодействует с сервисом пользователей через внедрённую зависимость.
 */
@Tag(name = "Admin: Пользователи", description = "Операции для управления пользователями (администратор)")
@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class ExternalUserController {

    private final UserService userService;

    /**
     * Создание нового пользователя.
     * <p>
     * Логирует успешное создание пользователя и возвращает его DTO.
     *
     * @param request данные нового пользователя
     * @return HTTP-ответ с пользователем и статусом CREATED
     */
    @Operation(summary = "Создать нового пользователя",
            description = "Позволяет администратору создать нового пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "401", description = "Нет доступа"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PostMapping
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid NewUserRequest request) {
        log.info("POST /admin/users with params: {}", request);
        UserDto createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Получение списка пользователей.
     * <p>
     * Логирует входящие параметры и возвращает список пользователей.
     *
     * @param ids   список идентификаторов пользователей
     * @param from  начальная позиция (смещение)
     * @param size  количество элементов на странице
     * @return HTTP-ответ со списком пользователей и статусом OK
     */
    @Operation(summary = "Получить список пользователей",
            description = "Возвращает список пользователей. Поддерживает фильтрацию по ID и пагинацию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "401", description = "Нет доступа"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /admin/users?ids={}&from={}&size={}", ids, from, size);
        List<UserDto> users = userService.getUsers(ids, from, size);
        log.info("Отправлен список пользователей с размером: {}", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Удаление пользователя по ID.
     * <p>
     * Логирует успешное удаление пользователя.
     *
     * @param userId идентификатор пользователя
     * @return HTTP-ответ без содержимого и статусом NO_CONTENT
     */
    @Operation(summary = "Удалить пользователя",
            description = "Позволяет администратору удалить пользователя по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}