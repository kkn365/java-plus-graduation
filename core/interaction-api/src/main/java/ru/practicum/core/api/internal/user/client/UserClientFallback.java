package ru.practicum.core.api.internal.user.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.util.Collections;
import java.util.List;

/**
 * Реализация fallback-поведения для клиента пользовательского сервиса.
 * <p>
 * Используется при сбое внешнего сервиса для обеспечения отказоустойчивости.
 */
@Component
@Slf4j
public class UserClientFallback implements UserClient {
    private static final String USER_FALLBACK_MSG = "Ошибка получения пользователя ID={}. Использован fallback.";
    private static final String USERS_FALLBACK_MSG = "Ошибка получения пользователей. Использован fallback.";

    /**
     * Метод получения информации о пользователе с fallback-обработкой.
     * <p>
     * При сбое внешнего сервиса возвращает статус NOT_FOUND.
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity с пустым телом и статусом NOT_FOUND
     * @throws FeignException при сбое обращения к сервису
     */
    @Override
    public ResponseEntity<UserShortDto> getUser(Long userId) throws FeignException {
        log.warn(USER_FALLBACK_MSG, userId);
        return ResponseEntity.notFound().build();
    }

    /**
     * Метод получения списка пользователей с fallback-обработкой.
     * <p>
     * При сбое внешнего сервиса возвращает пустой список.
     *
     * @param ids список идентификаторов пользователей
     * @return ResponseEntity с пустым списком и статусом OK
     * @throws FeignException при сбое обращения к сервису
     */
    @Override
    public ResponseEntity<List<UserShortDto>> getUsers(List<Long> ids) throws FeignException {
        log.warn(USERS_FALLBACK_MSG);
        return ResponseEntity.ok(Collections.emptyList());
    }
}