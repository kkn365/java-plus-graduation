package ru.practicum.core.api.internal.decoders;

import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки декодера ошибок Feign.
 * <p>
 * Используется для обработки HTTP-ошибок, возвращаемых микросервисами при взаимодействии через Feign-клиенты.
 * Позволяет преобразовывать исходные HTTP-ответы с ошибками в исключения, понятные логике приложения.
 */
@Configuration
public class ErrorDecoderConfig {
    /**
     * Регистрирует настраиваемый декодер ошибок для всех Feign-клиентов в приложении.
     * <p>
     * Использует {@link CustomErrorDecoder} для обработки ответов с HTTP-ошибками,
     * добавляя контекст и детали ошибки.
     */
    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .errorDecoder(new CustomErrorDecoder());
    }
}