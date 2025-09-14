package ru.practicum.core.event.dto.events;

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
    private String text;

    /**
     * Список идентификаторов категорий событий.
     */
    private List<Long> categories;

    /**
     * Признак платности события (true — только платные, false — бесплатные).
     */
    private Boolean paid;

    /**
     * Признак наличия свободных мест (true — только события с free slots).
     */
    private Boolean onlyAvailable;

    /**
     * Начало временного диапазона для фильтрации событий.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    private LocalDateTime rangeStart;

    /**
     * Конец временного диапазона для фильтрации событий.
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    private LocalDateTime rangeEnd;

    /**
     * Критерий сортировки событий.
     * Допустимые значения: EVENT_DATE (по дате), VIEWS (по просмотрам).
     */
    @Builder.Default
    private EventSort sort = EventSort.VIEWS;

    /**
     * Начальная позиция для пагинации (смещение).
     * По умолчанию: 0.
     */
    @Builder.Default
    private Integer from = Integer.valueOf(DEFAULT_FROM);

    /**
     * Размер страницы для пагинации.
     * По умолчанию: 10.
     */
    @Builder.Default
    private Integer size = Integer.valueOf(DEFAULT_SIZE);
}