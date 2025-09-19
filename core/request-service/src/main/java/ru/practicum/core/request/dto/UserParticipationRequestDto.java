package ru.practicum.core.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для представления результатов обработки заявок на участие в событии.
 * <p>
 * Содержит списки подтверждённых и отклонённых заявок.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления результата обработки заявок на участие в событии")
public class UserParticipationRequestDto {

    /**
     * Список подтверждённых заявок.
     */
    @Schema(description = "Список подтверждённых заявок", example = "[...]", type = "array")
    private List<ParticipationRequestDto> confirmedRequests;

    /**
     * Список отклонённых заявок.
     */
    @Schema(description = "Список отклонённых заявок", example = "[...]", type = "array")
    private List<ParticipationRequestDto> rejectedRequests;
}