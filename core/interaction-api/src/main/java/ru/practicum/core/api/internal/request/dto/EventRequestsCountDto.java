package ru.practicum.core.api.internal.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для передачи информации о количестве подтверждённых заявок на событие.
 * <p>
 * Используется при обмене данными между сервисами, например, между сервисом событий и сервисом заявок.
 */
@Data
@AllArgsConstructor
public class EventRequestsCountDto {

    /**
     * Уникальный идентификатор события.
     * <p>
     * Не может быть null, так как необходимо однозначно идентифицировать событие.
     */
    @NotNull(message = "ID события не может быть null")
    private Long eventId;

    /**
     * Количество подтверждённых заявок на участие в событии.
     * <p>
     * Может быть 0, если нет подтверждённых заявок.
     */
    private Long confirmedRequests;
}