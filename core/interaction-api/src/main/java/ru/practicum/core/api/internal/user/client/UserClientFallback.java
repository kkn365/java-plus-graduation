package ru.practicum.core.api.internal.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.util.Collections;
import java.util.List;

/**
 * Реализация fallback-логики для клиента пользовательского сервиса.
 * <p>
 * Используется при сбое внешнего сервиса (FeignException), чтобы вернуть структурированную ошибку
 * вместо возврата null или выброса исключения без контекста.
 */
@Component
@Slf4j
public class UserClientFallback implements UserClient {

    /**
     * Fallback-метод для получения пользователя по идентификатору.
     * <p>
     * В случае сбоя внешнего сервиса возвращает DTO с метаданными об ошибке,
     * а не null, что позволяет избежать NullPointerException и упрощает диагностику.
     *
     * @param userId Идентификатор пользователя
     * @return ResponseEntity с HTTP-статусом SERVICE_UNAVAILABLE и DTO с информацией об ошибке
     */
    @Override
    public ResponseEntity<UserShortDto> getUser(Long userId) {
        log.warn("Ошибка доступа к пользовательскому сервису при получении пользователя ID={}", userId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(UserShortDto.builder()
                        .id(userId)
                        .name("Сервис недоступен")
                        .build());
    }

    /**
     * Fallback-метод для получения списка пользователей.
     * <p>
     * Возвращает пустой список с флагом ошибки вместо null.
     *
     * @param userIds Список идентификаторов пользователей
     * @return ResponseEntity с HTTP-статусом SERVICE_UNAVAILABLE и пустым списком
     */
    @Override
    public ResponseEntity<List<UserShortDto>> getUsers(List<Long> userIds) {
        log.warn("Ошибка доступа к пользовательскому сервису при получении пользователей {}", userIds);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(List.of(UserShortDto.builder()
                        .id(0L)
                        .name("Сервис недоступен")
                        .build()));
    }
}