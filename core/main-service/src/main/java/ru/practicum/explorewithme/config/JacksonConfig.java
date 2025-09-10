package ru.practicum.explorewithme.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    /**
     * Регистрирует кастомные сериализатор и десериализатор для LocalDateTime.
     * <p>
     * Используется для поддержки пользовательского формата даты/времени в JSON:
     * yyyy-MM-dd HH:mm:ss.
     */
    @Bean
    public SimpleModule customJavaTimeModule() {
        SimpleModule module = new SimpleModule();
        // Добавляем кастомный десериализатор для LocalDateTime
        module.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        // Добавляем кастомный сериализатор для LocalDateTime
        module.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        return module;
    }
}