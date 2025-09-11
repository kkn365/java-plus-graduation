package ru.practicum.explorewithme.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.events.enumeration.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

/**
 * Параметры фильтрации событий для администратора.
 * <p>
 * Используется в методах получения событий с возможностью фильтрации по пользователям, категориям,
 * статусу и временному диапазону, а также поддерживает пагинацию.
 */
@Data
@Builder
public class AdminEventParams {
    /**
     * Список идентификаторов пользователей, чьи события будут включены в результат.
     * <p>
     * По умолчанию — пустой список.
     */
    @Builder.Default
    private List<Long> users = new ArrayList<>();

    /**
     * Список статусов событий для фильтрации.
     * <p>
     * Допустимые значения: {@link ru.practicum.explorewithme.events.enumeration.EventState}.
     * По умолчанию — пустой список.
     */
    @Builder.Default
    private List<EventState> states = new ArrayList<>();

    /**
     * Список идентификаторов категорий, к которым относятся события.
     * <p>
     * По умолчанию — пустой список.
     */
    @Builder.Default
    private List<Long> categories = new ArrayList<>();

    /**
     * Начало временного диапазона для фильтрации событий.
     * <p>
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    private LocalDateTime rangeStart;

    /**
     * Конец временного диапазона для фильтрации событий.
     * <p>
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    private LocalDateTime rangeEnd;

    /**
     * Начальная позиция для пагинации (смещение).
     * <p>
     * По умолчанию — 0.
     */
    @Builder.Default
    private Integer from = Integer.valueOf(DEFAULT_FROM);

    /**
     * Размер страницы для пагинации.
     * <p>
     * По умолчанию — 10.
     */
    @Builder.Default
    private Integer size = Integer.valueOf(DEFAULT_SIZE);
}