package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.dto.validator.ValidEndpoint;
import ru.practicum.dto.validator.ValidIpAddress;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateHitDTO {

    @NotBlank
    private String app;

    @NotNull
    @ValidEndpoint
    private String uri;

    @ValidIpAddress
    private String ip;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Date and time are required")
    private LocalDateTime timestamp;
}