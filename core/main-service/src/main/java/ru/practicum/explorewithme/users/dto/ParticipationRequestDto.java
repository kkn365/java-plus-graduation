package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.users.model.RequestStatus;

import java.time.LocalDateTime;

/**
 * DTO для представления заявки на участие в событии.
 * <p>
 * Содержит идентификатор заявки, события, пользователя, дату создания и статус.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    /**
     * Уникальный идентификатор заявки.
     */
    private Long id;

    /**
     * Идентификатор события, на которое подана заявка.
     * <p>
     * Обязательное поле.
     */
    @NotNull(message = "Идентификатор события не может быть пустым")
    private Long event;

    /**
     * Идентификатор пользователя, подавшего заявку.
     * <p>
     * Обязательное поле.
     */
    @NotNull(message = "Идентификатор пользователя не может быть пустым")
    private Long requester;

    /**
     * Дата и время создания заявки.
     */
    private LocalDateTime created;

    /**
     * Статус заявки (ожидание, подтверждён, отклонён, отменён).
     */
    private RequestStatus status;
}