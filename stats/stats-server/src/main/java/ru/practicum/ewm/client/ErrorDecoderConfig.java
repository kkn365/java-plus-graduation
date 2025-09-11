package ru.practicum.ewm.client;

import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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