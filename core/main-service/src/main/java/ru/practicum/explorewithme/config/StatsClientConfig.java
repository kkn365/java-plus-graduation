package ru.practicum.explorewithme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.hit.HitClient;
import ru.practicum.client.stats.StatsClient;

@Configuration
public class StatsClientConfig {

    /**
     * Конфигурация клиентов для взаимодействия с сервисом статистики.
     * <p>
     * Использует автоматическое обнаружение сервиса через Discovery Server.
     * Нет необходимости вручную указывать базовый URL — он будет получен динамически.
     */

    @Bean
    public HitClient hitClient() {
        return new HitClient();
    }

    @Bean
    public StatsClient statsClient() {
        return new StatsClient();
    }
}
