package ru.practicum.explorewithme.constraint.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import ru.practicum.explorewithme.constraint.EventStartDateTime;

import java.time.LocalDateTime;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class EventDateFromValidator implements ConstraintValidator<EventStartDateTime, LocalDateTime> {
    @Override
    public boolean isValid(
            LocalDateTime fieldValue,
            ConstraintValidatorContext constraintContext) {

        if (fieldValue == null) {
            return true;
        }

        return fieldValue.isAfter(LocalDateTime.now().plusHours(2L));
    }
}