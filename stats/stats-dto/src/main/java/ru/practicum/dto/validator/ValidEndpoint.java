package ru.practicum.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.dto.validator.constraint.EndpointValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для валидации корректности URI эндпоинта.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndpointValidator.class)
@Documented
public @interface ValidEndpoint {
    /**
     * Сообщение об ошибке при несоответствии формату.
     */
    String message() default "URI не соответствует формату";

    /**
     * Группы валидации, к которым относится эта аннотация.
     * Используется для условной валидации (например, группировка по бизнес-логике).
     */
    Class<?>[] groups() default {};

    /**
     * Дополнительные данные для пользовательских механизмов обработки ошибок.
     * Редко используется напрямую разработчиками.
     */
    Class<? extends Payload>[] payload() default {};
}