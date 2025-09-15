package ru.practicum.core.api.internal.request.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с сервисом заявок.
 * <p>
 * Предоставляет методы для получения статистики по количеству подтверждённых заявок на участие в событиях.
 */
@FeignClient(name = "request-service", path = "/internal/request")
public interface RequestClient {

    /**
     * Получает количество подтверждённых заявок на участие в мероприятиях по их идентификаторам.
     *
     * @param eventIds Список идентификаторов событий
     * @return ResponseEntity с HTTP-статусом 200 и телом, содержащим DTO с количеством заявок
     * @throws FeignException если произошла ошибка взаимодействия с внешним сервисом
     */
    @GetMapping("/count")
    ResponseEntity<List<EventRequestsCountDto>> getEventRequestsCount(@RequestParam List<Long> eventIds) throws FeignException;

    /**
     * Проверяет, подана ли заявка на участие в событии.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return true, если заявка подана, иначе false
     * @throws FeignException если произошла ошибка взаимодействия с внешним сервисом
     */
    @GetMapping("/has-request")
    ResponseEntity<Boolean> hasRequest(@RequestParam Long userId, @RequestParam Long eventId) throws FeignException;
}