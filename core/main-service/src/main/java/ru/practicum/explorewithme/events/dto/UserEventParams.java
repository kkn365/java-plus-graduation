package ru.practicum.explorewithme.events.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class UserEventParams {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private Boolean onlyAvailable;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Optional<EventSortEnum> sort; // todo: не рекомендуется так делать
    private Integer from;
    private Integer size;

    public enum EventSortEnum {
        EVENT_DATE,
        VIEWS
    }
}
