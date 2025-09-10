package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.users.model.RequestStatus;

import java.util.List;

/**
 * DTO для изменения статуса заявок на участие в событии.
 * <p>
 * Содержит список идентификаторов заявок и новый статус.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestStatusDto {

    /**
     * Список идентификаторов заявок, статус которых нужно изменить.
     * <p>
     * Обязательное поле. Минимум 1 идентификатор.
     */
    @NotNull(message = "Список заявок не может быть пустым")
    @Size(min = 1, message = "Минимум одна заявка должна быть указана")
    private List<Long> requestIds;

    /**
     * Новый статус для заявок.
     * <p>
     * Обязательное поле. Допустимые значения: CONFIRMED, REJECTED.
     */
    @NotNull(message = "Статус не может быть пустым")
    private RequestStatus status;
}