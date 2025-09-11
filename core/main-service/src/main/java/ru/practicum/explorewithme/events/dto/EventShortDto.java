package ru.practicum.explorewithme.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.users.dto.ShortUserDto;

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
    private String annotation;

    /**
     * Категория события.
     */
    private CategoryDto category;

    /**
     * Количество подтверждённых заявок на участие.
     */
    private Integer confirmedRequests;

    /**
     * Дата и время начала события.
     */
    private LocalDateTime eventDate;

    /**
     * Уникальный идентификатор события.
     */
    private Long id;

    /**
     * Инициатор события (пользователь).
     */
    private ShortUserDto initiator;

    /**
     * Признак платности события.
     */
    private Boolean paid;

    /**
     * Название события.
     */
    private String title;

    /**
     * Количество просмотров события.
     */
    private Long views;
}