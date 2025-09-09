package ru.practicum.explorewithme.events.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.config.CustomLocalDateTimeDeserializer;
import ru.practicum.explorewithme.config.CustomLocalDateTimeSerializer;
import ru.practicum.explorewithme.constraint.EventStartDateTime;
import ru.practicum.explorewithme.events.enumeration.EventStateAction;

import java.time.LocalDateTime;

@Data
@Builder
public class NewEventDto {
    @NotBlank(groups = {RequestMethod.Create.class})
    @Size(min = 3, max = 120, groups = {RequestMethod.Create.class, RequestMethod.Update.class})
    private String title;
    @NotBlank(groups = {RequestMethod.Create.class})
    @Size(min = 20, max = 2000, groups = {RequestMethod.Create.class, RequestMethod.Update.class})
    private String annotation;
    @NotBlank(groups = {RequestMethod.Create.class})
    @Size(min = 20, max = 7000, groups = {RequestMethod.Create.class, RequestMethod.Update.class})
    private String description;
    @NotNull(groups = {RequestMethod.Create.class})
    private Long category;
    @NotNull(groups = {RequestMethod.Create.class})
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class) // Для входящих данных
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)     // Для исходящих данных
    @EventStartDateTime(groups = {RequestMethod.Create.class, RequestMethod.Update.class})
    private LocalDateTime eventDate; // Формат: yyyy-MM-dd HH:mm:ss
    @NotNull(groups = {RequestMethod.Create.class})
    private LocationDto location;
    @PositiveOrZero(groups = {RequestMethod.Create.class, RequestMethod.Update.class})
    private Integer participantLimit;
    private Boolean paid;
    private Boolean requestModeration;
    private EventStateAction stateAction;
}
