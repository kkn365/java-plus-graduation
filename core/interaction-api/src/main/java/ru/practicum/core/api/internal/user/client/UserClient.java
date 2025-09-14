package ru.practicum.core.api.internal.user.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с пользовательским микросервисом.
 * <p>
 * Обеспечивает получение информации о пользователях по их идентификаторам.
 */
@FeignClient(name = "user-service", path = "/internal/user")
public interface UserClient {

    /**
     * Получает краткую информацию о пользователе по его идентификатору.
     *
     * @param userId Идентификатор пользователя
     * @return ResponseEntity с DTO пользователя или ошибкой
     * @throws FeignException если произошла ошибка при выполнении запроса
     */
    @GetMapping("/{userId}")
    ResponseEntity<UserShortDto> getUser(@PathVariable Long userId) throws FeignException;

    /**
     * Получает список краткой информации о нескольких пользователях по их идентификаторам.
     *
     * @param ids Список идентификаторов пользователей
     * @return ResponseEntity со списком DTO пользователей или ошибкой
     * @throws FeignException если произошла ошибка при выполнении запроса
     */
    @GetMapping
    ResponseEntity<List<UserShortDto>> getUsers(@RequestParam List<Long> ids) throws FeignException;
}