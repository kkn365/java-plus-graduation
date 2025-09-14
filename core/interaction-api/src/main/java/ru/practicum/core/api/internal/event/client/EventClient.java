package ru.practicum.core.api.internal.event.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.api.internal.event.dto.EventDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с событийным микросервисом.
 * <p>
 * Обеспечивает получение информации о событии по его идентификатору.
 */
@FeignClient(name = "event-service", path = "/internal/event")
public interface EventClient {

    /**
     * Получает информацию о событии по его идентификатору.
     *
     * @param eventId Идентификатор события
     * @return ResponseEntity с DTO события или ошибкой
     * @throws FeignException если произошла ошибка при выполнении запроса
     */
    @GetMapping("/{eventId}")
    ResponseEntity<EventDto> getEventById(@PathVariable("eventId") Long eventId) throws FeignException;

    @GetMapping()
    ResponseEntity<List<EventDto>> getAllEventsByInitiatorId(
            @RequestParam("initiatorId") Long initiatorId
    ) throws FeignException;
}