package ru.practicum.dto.validator.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.dto.validator.ValidIpAddress;

public class IpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return value != null && (value.matches(ipv4Pattern) || value.matches(ipv6Pattern));
    }
}
