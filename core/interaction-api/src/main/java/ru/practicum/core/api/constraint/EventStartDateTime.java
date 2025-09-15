package ru.practicum.core.api.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.core.api.constraint.validator.EventDateFromValidator;

import java.lang.annotation.*;

/**
 * Аннотация для валидации даты события.
 * <p>
 * Убедитесь, что указанная дата не ранее чем через 2 часа от текущего времени.
 * Используется в DTO создания события.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {EventDateFromValidator.class})
public @interface EventStartDateTime {

    /**
     * Сообщение об ошибке, если дата некорректна.
     * <p>
     * По умолчанию: "Дата события должна быть не ранее чем через два часа от текущего времени".
     */
    String message() default "Дата события должна быть не ранее чем через два часа от текущего времени";

    /**
     * Группа валидации (по умолчанию — пустой массив).
     * <p>
     * Используется для группировки проверок при необходимости.
     */
    Class<?>[] groups() default {};

    /**
     * Пользовательская нагрузка для сообщений об ошибках.
     * <p>
     * Может использоваться для дополнительной информации или логирования.
     */
    Class<? extends Payload>[] payload() default {};
}