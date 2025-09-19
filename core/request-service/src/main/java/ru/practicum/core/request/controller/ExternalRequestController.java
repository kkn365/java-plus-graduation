package ru.practicum.core.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import ru.practicum.core.request.dto.ChangeRequestStatusDto;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.dto.UserParticipationRequestDto;
import ru.practicum.core.request.service.RequestService;

/**
 * Контроллер для работы с заявками на участие в событиях.
 * <p>
 * Обрабатывает запросы на создание, отмену и изменение статуса заявок, а также получение списка заявок
 * пользователя или конкретного события.
 */
@Tag(name = "Заявки", description = "Операции для управления заявками на участие")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ExternalRequestController {

    private final RequestService requestService;

    /**
     * Получает список всех заявок текущего пользователя.
     *
     * @param userId Идентификатор пользователя
     * @return HTTP-ответ со списком DTO заявок и статусом OK
     */
    @Operation(summary = "Получить список заявок пользователя",
            description = "Возвращает все заявки, поданные текущим пользователем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> findAllRequests(@PathVariable long userId) {
        log.info("GET /users/{}/requests", userId);
        List<ParticipationRequestDto> requests = requestService.getAllRequestsByUser(userId);
        log.info("Найдено {} заявок для пользователя {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Создаёт новую заявку на участие в событии.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return HTTP-ответ с созданной DTO заявкой и статусом CREATED
     */
    @Operation(summary = "Создать заявку на участие",
            description = "Позволяет пользователю подать заявку на участие в событии.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заявка успешно создана",
                    content = @Content(schema = @Schema(implementation = ParticipationRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Событие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> save(
            @PathVariable long userId,
            @RequestParam("eventId") @NotNull long eventId) {
        log.info("POST /users/{}/requests&eventId={}", userId, eventId);
        ParticipationRequestDto createdRequest = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    /**
     * Отменяет существующую заявку пользователя.
     *
     * @param userId     Идентификатор пользователя
     * @param requestId  Идентификатор заявки
     * @return HTTP-ответ с обновлённой DTO заявкой и статусом OK
     */
    @Operation(summary = "Отменить заявку",
            description = "Позволяет пользователю отменить ранее поданную заявку.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка успешно отменена",
                    content = @Content(schema = @Schema(implementation = ParticipationRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable long userId,
            @PathVariable long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        ParticipationRequestDto updatedRequest = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Получает список заявок на конкретное событие, принадлежащее пользователю.
     *
     * @param userId   Идентификатор пользователя
     * @param eventId  Идентификатор события
     * @return HTTP-ответ со списком DTO заявок и статусом OK
     */
    @Operation(summary = "Получить заявки на событие",
            description = "Возвращает список заявок на конкретное событие, принадлежащее пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "404", description = "Событие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> findUserRequestsOnEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        List<ParticipationRequestDto> requests = requestService.getUserRequestsForEvent(userId, eventId);
        log.info("Найдено {} заявок на событие {}", requests.size(), eventId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Изменяет статус заявок (подтверждение/отказ).
     *
     * @param changeRequestStatusDto DTO с параметрами изменения статуса
     * @param userId                 Идентификатор пользователя
     * @param eventId                Идентификатор события
     * @return HTTP-ответ с результатами обработки заявок и статусом OK
     */
    @Operation(summary = "Изменить статус заявок",
            description = "Позволяет изменить статус заявок (подтверждение или отказ) на участие в событии.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заявок успешно изменён",
                    content = @Content(schema = @Schema(implementation = UserParticipationRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Событие или заявки не найдены"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PatchMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<UserParticipationRequestDto> patchRequestStatus(
            @Valid @RequestBody ChangeRequestStatusDto changeRequestStatusDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("PATCH /users/{}/events/{}/requests: {}", userId, eventId, changeRequestStatusDto);
        UserParticipationRequestDto result = requestService.updateRequestStatus(changeRequestStatusDto, userId, eventId);
        log.info("Обработано {} подтверждённых и {} отклонённых заявок",
                result.getConfirmedRequests().size(),
                result.getRejectedRequests().size());
        return ResponseEntity.ok(result);
    }
}