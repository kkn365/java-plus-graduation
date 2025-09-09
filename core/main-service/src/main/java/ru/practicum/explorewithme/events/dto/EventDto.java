package ru.practicum.explorewithme.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.users.dto.ShortUserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private ShortUserDto initiator;
    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private LocationDto location;
    private Integer participantLimit;
    private Boolean paid;
    private Boolean requestModeration;
    private String state;
    private Integer confirmedRequests;
    private Long views;
}
