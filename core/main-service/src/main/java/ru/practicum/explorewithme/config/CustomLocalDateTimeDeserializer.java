package ru.practicum.explorewithme.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.explorewithme.util.DateTimeFormatConstants.DATE_TIME_FORMAT;

/**
 * Пользовательский десериализатор для преобразования строк в формате дата-время в объекты {@link LocalDateTime}.
 * <p>
 * Используется для обработки нестандартных или локализованных форматов дат, отличных от ISO 8601.
 */
public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
        final String value = p.getValueAsString();

        // Обработка null и пустых значений
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(p.getValueAsString(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        } catch (Exception e) {
            throw new IOException("Неверный формат даты: должно быть " + DATE_TIME_FORMAT, e);
        }
    }
}