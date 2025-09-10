package ru.practicum.explorewithme.users.controller;

import jakarta.validation.Valid;
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
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.service.UserService;

import java.util.List;

import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * Создание нового пользователя.
     * <p>
     * Логирует успешное создание пользователя и возвращает его DTO.
     *
     * @param request данные нового пользователя
     * @return HTTP-ответ с пользователем и статусом CREATED
     */
    @PostMapping
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid NewUserRequest request) {
        log.info("Получен POST-запрос на создание пользователя с данными: {}", request);
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
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        log.info("Получен GET-запрос на получение пользователей: ids={}, from={}, size={}", ids, from, size);
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
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("Получен DELETE-запрос на удаление пользователя с ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}