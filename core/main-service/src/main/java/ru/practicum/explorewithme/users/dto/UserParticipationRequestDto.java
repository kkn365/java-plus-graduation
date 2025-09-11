package ru.practicum.explorewithme.users.dto;

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
public class UserParticipationRequestDto {

    /**
     * Список подтверждённых заявок.
     */
    private List<ParticipationRequestDto> confirmedRequests;

    /**
     * Список отклонённых заявок.
     */
    private List<ParticipationRequestDto> rejectedRequests;
}
