package ru.practicum.explorewithme.users.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventRequestCount {
    private Long eventId;
    private Long requestsCount;
}