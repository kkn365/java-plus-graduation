package ru.practicum.core.event.dto.compilations;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.event.dto.events.EventShortDto;

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
    @Schema(description = "Уникальный идентификатор подборки", example = "1")
    private Long id;

    /**
     * Заголовок подборки.
     */
    @Schema(description = "Заголовок подборки", example = "Популярные события")
    private String title;

    /**
     * Флаг закрепления: true — отображается на главной странице.
     */
    @Schema(description = "Флаг закрепления (true — отображается на главной странице)", example = "true")
    private Boolean pinned;

    /**
     * Список событий, входящих в подборку.
     * <p>
     * Содержит упрощённое представление события (EventShortDto).
     */
    @Schema(
            description = "Список событий в подборке",
            example = "[...]",
            implementation = EventShortDto.class)
    private List<EventShortDto> events;
}