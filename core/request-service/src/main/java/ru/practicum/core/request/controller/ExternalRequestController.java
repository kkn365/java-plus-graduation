package ru.practicum.core.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.dto.ChangeRequestStatusDto;
import ru.practicum.core.request.dto.UserParticipationRequestDto;
import ru.practicum.core.request.service.RequestService;

import java.util.List;

/**
 * Контроллер для работы с заявками на участие в событиях.
 * <p>
 * Обрабатывает запросы на создание, отмену и изменение статуса заявок, а также получение списка заявок
 * пользователя или конкретного события.
 */
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