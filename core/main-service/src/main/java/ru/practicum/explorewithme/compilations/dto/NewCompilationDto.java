package ru.practicum.explorewithme.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {

    private Set<Long> events;

    private Boolean pinned = false;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
    private String title;
}