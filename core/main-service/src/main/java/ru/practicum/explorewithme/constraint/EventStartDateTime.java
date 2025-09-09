package ru.practicum.explorewithme.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.explorewithme.constraint.validator.EventDateFromValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {
        EventDateFromValidator.class
})
public @interface EventStartDateTime {
    String message() default "Datetime must be more then 2 hours from now()";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

