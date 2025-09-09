package ru.practicum.explorewithme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.hit.HitClient;
import ru.practicum.client.stats.StatsClient;


@Configuration
public class StatsClientConfig {

    @Bean
    public HitClient hitClient() {
        return new HitClient();
    }

    @Bean
    public StatsClient statsClient() {
        return new StatsClient();
    }
}
