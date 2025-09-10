package ru.practicum.explorewithme.events.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.events.enumeration.EventState;
import ru.practicum.explorewithme.users.dto.ShortUserDto;

import java.time.LocalDateTime;

/**
 * DTO для представления события в системе.
 * <p>
 * Содержит основную информацию о событии, включая метаданные, статистику и связи с другими сущностями.
 */
@Data
@Builder
public class EventDto {
    /**
     * Уникальный идентификатор события.
     */
    private Long id;

    /**
     * Название события.
     * <p>
     * Не может быть пустым, длина от 3 до 120 символов.
     */
    @NotNull(message = "Название события не может быть пустым")
    private String title;

    /**
     * Краткое описание события (аннотация).
     * <p>
     * Не может быть пустым, длина от 20 до 2000 символов.
     */
    @NotNull(message = "Аннотация события не может быть пустой")
    private String annotation;

    /**
     * Полное описание события.
     * <p>
     * Не может быть пустым, длина от 20 до 7000 символов.
     */
    @NotNull(message = "Описание события не может быть пустым")
    private String description;

    /**
     * Категория события.
     * <p>
     * Связь с моделью {@link CategoryDto}.
     */
    private CategoryDto category;

    /**
     * Инициатор события (пользователь).
     * <p>
     * Связь с моделью {@link ShortUserDto}.
     */
    private ShortUserDto initiator;

    /**
     * Дата и время начала события.
     * <p>
     * Формат: yyyy-MM-dd HH:mm:ss.
     */
    @NotNull(message = "Дата события обязательна")
    private LocalDateTime eventDate;

    /**
     * Дата и время создания события.
     * <p>
     * Заполняется автоматически при создании.
     */
    private LocalDateTime createdOn;

    /**
     * Дата и время публикации события.
     * <p>
     * Может быть null, если событие ещё не опубликовано.
     */
    private LocalDateTime publishedOn;

    /**
     * Геолокация события.
     * <p>
     * Содержит широту и долготу места проведения.
     */
    private LocationDto location;

    /**
     * Лимит участников события.
     * <p>
     * По умолчанию 0, значение должно быть >= 0.
     */
    private Integer participantLimit;

    /**
     * Признак платности события.
     * <p>
     * По умолчанию false.
     */
    private Boolean paid;

    /**
     * Признак необходимости модерации заявок.
     * <p>
     * По умолчанию false.
     */
    private Boolean requestModeration;

    /**
     * Текущее состояние события.
     * <p>
     * Возможные значения: PENDING, PUBLISHED, CANCELED.
     */
    private EventState state;

    /**
     * Количество подтверждённых заявок на участие.
     * <p>
     * Всегда >= 0.
     */
    private Integer confirmedRequests;

    /**
     * Количество просмотров события.
     * <p>
     * Получается из внешнего сервиса статистики.
     */
    private Long views;
}