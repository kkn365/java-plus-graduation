package ru.practicum.core.event.dto.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.event.model.enums.events.EventSort;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.core.api.util.constants.PaginationConstants.DEFAULT_SIZE;

/**
 * Параметры фильтрации событий для пользовательского запроса.
 * <p>
 * Поддерживает фильтрацию по тексту, категориям, платности, доступности,
 * временному диапазону и пагинации. Результат может быть отсортирован.
 */
@Data
@Builder
public class UserEventParams {

    /**
     * Текст для поиска в заголовке и описании события.
     */
    @Schema(description = "Текст для поиска в заголовке и описании события", example = "концерт", nullable = true)
    private String text;

    /**
     * Список идентификаторов категорий событий.
     */
    @Schema(description = "Список идентификаторов категорий", example = "[100, 200]", nullable = true)
    private List<Long> categories;

    /**
     * Признак платности события (true — только платные, false — бесплатные).
     */
    @Schema(description = "Признак платности события", example = "true", nullable = true)
    private Boolean paid;

    /**
     * Признак наличия свободных мест (true — только события с free slots).
     */
    @Schema(description = "Показывать только события с доступными местами", example = "true", nullable = true)
    private Boolean onlyAvailable;

    /**
     * Начало временного диапазона для фильтрации событий.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @Schema(description = "Начало временного диапазона", example = "2025-04-01T10:00:00", nullable = true)
    private LocalDateTime rangeStart;

    /**
     * Конец временного диапазона для фильтрации событий.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @Schema(description = "Конец временного диапазона", example = "2025-04-07T23:59:59", nullable = true)
    private LocalDateTime rangeEnd;

    /**
     * Критерий сортировки событий.
     * Допустимые значения: EVENT_DATE (по дате), RATING (по рейтингу).
     */
    @Schema(description = "Критерий сортировки", example = "RATING", allowableValues = {"EVENT_DATE", "RATING"}, required = true)
    @Builder.Default
    private EventSort sort = EventSort.RATING;

    /**
     * Начальная позиция для пагинации (смещение).
     * По умолчанию: 0.
     */
    @Schema(description = "Смещение для пагинации", example = "0", minimum = "0", defaultValue = "0")
    @Builder.Default
    private Integer from = Integer.valueOf(DEFAULT_FROM);

    /**
     * Размер страницы для пагинации.
     * По умолчанию: 10.
     */
    @Schema(description = "Размер страницы", example = "10", minimum = "1", defaultValue = "10")
    @Builder.Default
    private Integer size = Integer.valueOf(DEFAULT_SIZE);
}