package ru.practicum.explorewithme.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.events.dto.EventShortDto;

import java.util.List;

/**
 * DTO для представления подборки событий.
 * <p>
 * Содержит заголовок, флаг закрепления и список событий.
 * Используется при возврате данных клиенту.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {
    /**
     * Уникальный идентификатор подборки.
     */
    private Long id;

    /**
     * Заголовок подборки.
     */
    private String title;

    /**
     * Флаг закрепления: true — отображается на главной странице.
     */
    private Boolean pinned;

    /**
     * Список событий, входящих в подборку.
     * <p>
     * Содержит упрощённое представление события (EventShortDto).
     */
    private List<EventShortDto> events;
}