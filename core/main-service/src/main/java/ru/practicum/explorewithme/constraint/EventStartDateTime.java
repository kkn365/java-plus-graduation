package ru.practicum.explorewithme.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.explorewithme.constraint.validator.EventDateFromValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Аннотация для валидации даты события.
 * <p>
 * Убедитесь, что указанная дата не ранее чем через 2 часа от текущего времени.
 * Используется в DTO создания события.
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {EventDateFromValidator.class})
public @interface EventStartDateTime {
    /**
     * Сообщение об ошибке, если дата некорректна.
     * <p>
     * По умолчанию: "Дата события должна быть не ранее чем через два часа от текущего времени".
     */
    String message() default "Дата события должна быть не ранее чем через два часа от текущего времени";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}