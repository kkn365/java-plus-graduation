package ru.practicum.core.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.api.util.enums.RequestStatus;

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
@Schema(description = "DTO для изменения статуса заявок (CONFIRMED / REJECTED)")
public class ChangeRequestStatusDto {

    /**
     * Список идентификаторов заявок, статус которых нужно изменить.
     * <p>
     * Обязательное поле. Минимум 1 идентификатор.
     */
    @NotNull(message = "Список заявок не может быть пустым")
    @Size(min = 1, message = "Минимум одна заявка должна быть указана")
    @Schema(
            description = "Список идентификаторов заявок",
            example = "[1, 2, 3]",
            required = true)
    private List<Long> requestIds;

    /**
     * Новый статус для заявок.
     * <p>
     * Обязательное поле. Допустимые значения: CONFIRMED, REJECTED.
     */
    @NotNull(message = "Статус не может быть пустым")
    @Schema(
            description = "Новый статус заявки",
            example = "CONFIRMED",
            allowableValues = {"CONFIRMED", "REJECTED"},
            required = true)
    private RequestStatus status;
}