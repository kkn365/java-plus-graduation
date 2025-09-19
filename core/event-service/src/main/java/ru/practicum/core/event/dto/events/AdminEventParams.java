package ru.practicum.core.event.dto.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.api.util.enums.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.core.api.util.constants.PaginationConstants.DEFAULT_SIZE;

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
    @Schema(description = "Список идентификаторов пользователей", example = "[1, 2, 3]", nullable = true)
    @Builder.Default
    private List<Long> users = new ArrayList<>();

    /**
     * Список статусов событий для фильтрации.
     * <p>
     * Допустимые значения: {@link EventState}.
     * По умолчанию — пустой список.
     */
    @Schema(
            description = "Список статусов событий",
            example = "[PENDING, PUBLISHED]",
            allowableValues = {"PENDING", "PUBLISHED", "CANCELED"},
            nullable = true)
    @Builder.Default
    private List<EventState> states = new ArrayList<>();

    /**
     * Список идентификаторов категорий, к которым относятся события.
     * <p>
     * По умолчанию — пустой список.
     */
    @Schema(description = "Список идентификаторов категорий", example = "[100, 200]", nullable = true)
    @Builder.Default
    private List<Long> categories = new ArrayList<>();

    /**
     * Начало временного диапазона для фильтрации событий.
     * <p>
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @Schema(description = "Начало временного диапазона", example = "2025-04-01T10:00:00", nullable = true)
    private LocalDateTime rangeStart;

    /**
     * Конец временного диапазона для фильтрации событий.
     * <p>
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @Schema(description = "Конец временного диапазона", example = "2025-04-07T23:59:59", nullable = true)
    private LocalDateTime rangeEnd;

    /**
     * Начальная позиция для пагинации (смещение).
     * <p>
     * По умолчанию — 0.
     */
    @Schema(description = "Смещение для пагинации", example = "0", minimum = "0", defaultValue = "0")
    @Builder.Default
    private Integer from = Integer.valueOf(DEFAULT_FROM);

    /**
     * Размер страницы для пагинации.
     * <p>
     * По умолчанию — 10.
     */
    @Schema(description = "Размер страницы", example = "10", minimum = "1", defaultValue = "10")
    @Builder.Default
    private Integer size = Integer.valueOf(DEFAULT_SIZE);
}