package ru.practicum.dto.validator.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.dto.validator.ValidIpAddress;

/**
 * Валидатор для проверки корректности IPv4 и IPv6 адресов.
 */
public class IpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {

    // Паттерн для IPv4 без ведущих нулей (кроме самого нуля)
    private static final String IPV4_PATTERN =
            "^" +
            "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])" +  // 0-255
            "\\." +
            "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])" +
            "\\." +
            "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])" +
            "\\." +
            "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])" +
            "$";

    // Паттерн для IPv6 с поддержкой сжатия (::)
    private static final String IPV6_PATTERN =
            "^" +
            "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +             // Полный адрес
            "([0-9a-fA-F]{1,4}:){1,7}:|" +                            // Сжатие в начале
            "(:[0-9a-fA-F]{1,4}){1,7}$|" +                            // Сжатие в конце
            "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +            // Сжатие посередине
            "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +     // Два сегмента
            "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +     // Три сегмента
            "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +     // Четыре сегмента
            "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +     // Пять сегментов
            "([0-9a-fA-F]{1,4}:){1}(:[0-9a-fA-F]{1,4}){1,6}|" +       // Шесть сегментов
            ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +                        // Только сжатие
            "[0-9a-fA-F]{1,4}((:[0-9a-fA-F]{1,4}){1,6}:)?)" +         // Сжатие в середине
            "(:[0-9a-fA-F]{1,4})?$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null &&
               (value.matches(IPV4_PATTERN) ||
                value.matches(IPV6_PATTERN));
    }
}