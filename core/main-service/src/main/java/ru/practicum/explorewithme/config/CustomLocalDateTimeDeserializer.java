package ru.practicum.explorewithme.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateString = parser.getText(); // Получаем строку из JSON
        try {
            return LocalDateTime.parse(dateString, FORMATTER); // Парсим строку в LocalDateTime
        } catch (Exception e) {
            throw new IOException("Failed to parse date: " + dateString, e);
        }
    }
}