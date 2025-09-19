package ru.practicum.core.request.controller;

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
import ru.practicum.core.api.internal.request.client.RequestClient;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.request.service.RequestService;

import java.util.List;

/**
 * Контроллер для обработки внутренних запросов, связанных с заявками на участие в событиях.
 * <p>
 * Предоставляет методы для получения статистики по количеству заявок на конкретные события.
 */
@Tag(name = "Internal: Заявки", description = "Операции для работы с заявками (внутренний доступ)")
@Slf4j
@RestController
@RequestMapping("/internal/request")
@RequiredArgsConstructor
public class InternalRequestController implements RequestClient {

    private final RequestService requestService;

    /**
     * Получает количество заявок на участие в мероприятиях по их идентификаторам.
     *
     * @param eventIds Список идентификаторов событий
     * @return ResponseEntity с HTTP-статусом 200 и телом, содержащим DTO с количеством заявок
     * @throws FeignException если произошла ошибка взаимодействия с внешним сервисом
     */
    @Operation(summary = "Получить количество заявок на мероприятия",
            description = "Возвращает количество заявок на участие в мероприятиях по их идентификаторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Список количества заявок успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Override
    public ResponseEntity<List<EventRequestsCountDto>> getEventRequestsCount(List<Long> eventIds) throws FeignException {
        log.info("GET /internal/request?eventIds={} - Получен внутренний запрос на получение количества заявок " +
                "на участие в мероприятиях", eventIds);
        List<EventRequestsCountDto> requestsCount = requestService.getEventRequestsCount(eventIds);
        return ResponseEntity.ok(requestsCount);
    }

    /**
     * Проверяет, подана ли заявка на участие в событии.
     *
     * @param userId  Идентификатор пользователя
     * @param eventId Идентификатор события
     * @return true, если заявка подана, иначе false
     * @throws FeignException если произошла ошибка взаимодействия с внешним сервисом
     */
    @Operation(summary = "Проверить наличие заявки",
            description = "Проверяет, подал ли пользователь заявку на участие в указанном мероприятии.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат проверки успешно получен",
                    content = @Content(schema = @Schema(type = "boolean"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Override
    public ResponseEntity<Boolean> hasRequest(Long userId, Long eventId) throws FeignException {
        log.info("GET /internal/request/has-request?userId={}&eventId={} - " +
                "Получен внутренний запрос на проверку наличия заявки на участие в мероприятии", userId, eventId);
        return ResponseEntity.ok(requestService.hasRequest(userId, eventId));
    }
}