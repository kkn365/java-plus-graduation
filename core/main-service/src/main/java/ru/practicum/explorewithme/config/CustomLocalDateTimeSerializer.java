package ru.practicum.explorewithme.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static ru.practicum.explorewithme.util.DateTimeFormatConstants.DATE_TIME_FORMAT;

/**
 * Пользовательский сериализатор для преобразования объектов {@link LocalDateTime} в строку с заданным форматом.
 * <p>
 * Используется для вывода дат и времени в формате: yyyy-MM-dd HH:mm:ss.
 */
public class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();  // Обработка null-значений
            return;
        }

        try {
            // Форматирование даты с использованием указанного шаблона
            String formattedDate = value.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            gen.writeString(formattedDate);
        } catch (DateTimeParseException e) {
            throw new IOException("Ошибка форматирования даты: " + value + ". Ожидаемый формат: " + DATE_TIME_FORMAT, e);
        }
    }
}