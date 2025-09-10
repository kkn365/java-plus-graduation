package ru.practicum.explorewithme.events.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.constraint.EventStartDateTime;
import ru.practicum.explorewithme.events.enumeration.EventStateAction;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateEventAdminRequest {

    /**
     * Краткое описание события (аннотация).
     * <p>
     * Длина от 20 до 2000 символов.
     */
    @Nullable
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    private String annotation;

    /**
     * Идентификатор категории события.
     * <p>
     * Обязательное поле при создании события, но может быть изменено администратором.
     */
    @Nullable
    private Long category;

    /**
     * Полное описание события.
     * <p>
     * Длина от 20 до 7000 символов.
     */
    @Nullable
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    private String description;

    /**
     * Дата и время начала события.
     * <p>
     * Формат: yyyy-MM-dd HH:mm:ss.
     * Должна быть не ранее чем через час после публикации.
     */
    @Nullable
    @EventStartDateTime
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    /**
     * Геолокация события.
     * <p>
     * Содержит широту и долготу места проведения.
     */
    @Nullable
    private LocationDto location;

    /**
     * Признак платности события.
     * <p>
     * true — событие платное, false — бесплатное.
     */
    @Nullable
    private Boolean paid;

    /**
     * Лимит участников события.
     * <p>
     * Значение должно быть >= 0.
     */
    @Nullable
    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    /**
     * Признак необходимости модерации заявок.
     * <p>
     * True — требуется модерация, false — автоматическое подтверждение.
     */
    @Nullable
    private Boolean requestModeration;

    /**
     * Действие над состоянием события.
     * <p>
     * Допустимые значения:
     * - PUBLISH_EVENT (опубликовать)
     * - REJECT_EVENT (отклонить)
     * - SEND_TO_REVIEW (отправить на повторную проверку)
     * - CANCEL_REVIEW (отменить отправку на проверку)
     */
    @Nullable
    private EventStateAction stateAction;

    /**
     * Название события.
     * <p>
     * Длина от 3 до 120 символов.
     */
    @Nullable
    @Size(min = 3, max = 120, message = "Название события должно содержать от 3 до 120 символов")
    private String title;
}