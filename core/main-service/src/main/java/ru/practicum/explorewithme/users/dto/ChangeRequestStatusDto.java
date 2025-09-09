package ru.practicum.explorewithme.users.dto;

import lombok.Data;
import ru.practicum.explorewithme.users.model.RequestStatus;

import java.util.List;

@Data
public class ChangeRequestStatusDto {
    List<Long> requestIds;
    RequestStatus status;
}
