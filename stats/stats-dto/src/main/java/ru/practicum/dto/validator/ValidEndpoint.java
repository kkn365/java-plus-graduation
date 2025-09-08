package ru.practicum.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.dto.validator.constraint.EndpointValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndpointValidator.class)
@Documented
public @interface ValidEndpoint {
    String message() default "Invalid endpoint format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}