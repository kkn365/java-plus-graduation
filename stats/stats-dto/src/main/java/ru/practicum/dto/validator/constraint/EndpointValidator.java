package ru.practicum.dto.validator.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.dto.validator.ValidEndpoint;

import java.util.regex.Pattern;


public class EndpointValidator implements ConstraintValidator<ValidEndpoint, String> {

    private static final Pattern ENDPOINT_PATTERN =
            Pattern.compile("^/[a-zA-Z0-9/-]+(?:/[a-zA-Z0-9/-]+)*(?:[?][a-zA-Z0-9]+(?:=[a-zA-Z0-9,&]+)*(?:&[a-zA-Z0-9]+(?:=[a-zA-Z0-9,&]+)*)*)?$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return ENDPOINT_PATTERN.matcher(value).matches();
    }
}