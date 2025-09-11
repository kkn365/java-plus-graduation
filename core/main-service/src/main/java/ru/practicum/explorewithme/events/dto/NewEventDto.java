package ru.practicum.explorewithme.events.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.config.CustomLocalDateTimeDeserializer;
import ru.practicum.explorewithme.config.CustomLocalDateTimeSerializer;
import ru.practicum.explorewithme.constraint.EventStartDateTime;
import ru.practicum.explorewithme.events.enumeration.EventStateAction;

import java.time.LocalDateTime;

/**
 * DTO для создания нового события.
 * <p>
 * Содержит обязательные и опциональные поля, необходимые для создания события.
 * Используется в методах POST /events.
 */
@Data
@Builder
public class NewEventDto {
    /**
     * Заголовок события.
     * <p>
     * Обязательное поле при создании. Длина от 3 до 120 символов.
     */
    @NotBlank(message = "Заголовок события не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок события должен содержать от 3 до 120 символов")
    private String title;

    /**
     * Краткое описание события (аннотация).
     * <p>
     * Обязательное поле при создании. Длина от 20 до 2000 символов.
     */
    @NotBlank(message = "Аннотация события не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация события должна содержать от 20 до 2000 символов")
    private String annotation;

    /**
     * Полное описание события.
     * <p>
     * Обязательное поле при создании. Длина от 20 до 7000 символов.
     */
    @NotBlank(message = "Описание события не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание события должно содержать от 20 до 7000 символов")
    private String description;

    /**
     * Идентификатор категории события.
     * <p>
     * Обязательное поле при создании.
     */
    @NotNull(message = "Категория события обязательна")
    private Long category;

    /**
     * Дата и время начала события.
     * <p>
     * Обязательное поле при создании. Формат: yyyy-MM-dd HH:mm:ss.
     * Должна быть не ранее чем через час после отправки запроса.
     */
    @NotNull(message = "Дата события обязательна")
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class) // Для входящих данных
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)     // Для исходящих данных
    @EventStartDateTime
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    /**
     * Геолокация события.
     * <p>
     * Обязательное поле при создании.
     */
    @NotNull(message = "Геолокация события обязательна")
    private LocationDto location;

    /**
     * Лимит участников события.
     * <p>
     * Необязательное поле, по умолчанию 0.
     * Значение должно быть >= 0.
     */
    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    /**
     * Признак платности события.
     * <p>
     * Необязательное поле, по умолчанию false.
     */
    private Boolean paid;

    /**
     * Признак необходимости модерации заявок.
     * <p>
     * Необязательное поле, по умолчанию true.
     */
    private Boolean requestModeration;

    /**
     * Действие над состоянием события.
     * <p>
     * Необязательное поле. Может принимать значения:
     * - SEND_TO_REVIEW
     * - CANCEL_REVIEW
     * - PUBLISH_EVENT
     * - REJECT_EVENT
     */
    private EventStateAction stateAction;
}