package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.dto.validator.ValidEndpoint;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HitsStatDTO {

    @NotBlank
    private String app;

    @NotNull
    @ValidEndpoint
    private String uri;

    @NotNull
    private Long hits;
}