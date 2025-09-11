package ru.practicum.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.dto.validator.constraint.IpAddressValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для валидации корректности IP-адреса (IPv4 или IPv6).
 * <p>
 * Используется для проверки, что значение поля соответствует формату IPv4 или IPv6.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IpAddressValidator.class)
public @interface ValidIpAddress {
    /**
     * Сообщение об ошибке при несоответствии формату.
     * По умолчанию: "Must be a valid IPv4 or IPv6 address".
     */
    String message() default "Must be a valid IPv4 or IPv6 address";

    /**
     * Группы валидации, к которым относится эта аннотация.
     * Используется для условной валидации.
     */
    Class<?>[] groups() default {};

    /**
     * Дополнительные данные для пользовательских механизмов обработки ошибок.
     * Редко используется напрямую разработчиками.
     */
    Class<? extends Payload>[] payload() default {};
}