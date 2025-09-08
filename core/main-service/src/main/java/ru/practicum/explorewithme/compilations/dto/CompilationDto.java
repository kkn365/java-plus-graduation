package ru.practicum.explorewithme.compilations.dto;

import lombok.*;
import ru.practicum.explorewithme.events.dto.EventDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventDto> events;
}