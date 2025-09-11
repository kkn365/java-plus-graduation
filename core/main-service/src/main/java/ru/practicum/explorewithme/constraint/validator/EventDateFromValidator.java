package ru.practicum.explorewithme.constraint.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import ru.practicum.explorewithme.constraint.EventStartDateTime;

import java.time.LocalDateTime;

/**
 * Валидатор для проверки даты события.
 * <p>
 * Убеждается, что указанная дата не ранее чем через 2 часа от текущего времени.
 */
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class EventDateFromValidator implements ConstraintValidator<EventStartDateTime, LocalDateTime> {

    private static final long TWO_HOURS = 2;

    @Override
    public boolean isValid(
            LocalDateTime fieldValue,
            ConstraintValidatorContext constraintContext) {

        // Если поле равно null, валидация пропускается
        if (fieldValue == null) {
            return true;
        }

        // Получаем текущее время с учётом временной зоны (если требуется)
        LocalDateTime nowPlusTwoHours = LocalDateTime.now().plusHours(TWO_HOURS);

        // Проверяем, что дата события после установленного порога
        return fieldValue.isAfter(nowPlusTwoHours);
    }
}