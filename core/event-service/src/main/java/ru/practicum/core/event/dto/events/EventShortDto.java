package ru.practicum.core.event.dto.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.time.LocalDateTime;

/**
 * DTO для краткого представления события.
 * <p>
 * Используется при возврате данных клиенту. Содержит основные поля события,
 * такие как заголовок, аннотация, категория и количество просмотров.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortDto {

    /**
     * Краткое описание события (аннотация).
     */
    @Schema(description = "Краткое описание события", example = "Интересное мероприятие для всех возрастов")
    private String annotation;

    /**
     * Категория события.
     */
    @Schema(description = "Категория события", implementation = CategoryDto.class)
    private CategoryDto category;

    /**
     * Количество подтверждённых заявок на участие.
     */
    @Schema(description = "Количество подтверждённых заявок", example = "150")
    private Integer confirmedRequests;

    /**
     * Дата и время начала события.
     */
    @Schema(description = "Дата и время начала события", example = "2025-04-10T14:00:00")
    private LocalDateTime eventDate;

    /**
     * Уникальный идентификатор события.
     */
    @Schema(description = "Уникальный идентификатор события", example = "1001")
    private Long id;

    /**
     * Инициатор события (пользователь).
     */
    @Schema(description = "Инициатор события", implementation = UserShortDto.class)
    private UserShortDto initiator;

    /**
     * Признак платности события.
     */
    @Schema(description = "Признак платности события", example = "true")
    private Boolean paid;

    /**
     * Название события.
     */
    @Schema(description = "Название события", example = "Международный фестиваль искусств")
    private String title;

    /**
     * Рейтинг события.
     */
    @Schema(description = "Рейтинг события", example = "4.7")
    private Double rating;
}