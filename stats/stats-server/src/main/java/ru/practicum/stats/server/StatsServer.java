package ru.practicum.stats.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {
        "ru.practicum.stats.server",
        "ru.practicum.stats.dto",
        "ru.practicum.stats.client",
        "ru.practicum.core.api"
})
public class StatsServer {
    public static void main(String[] args) {
        SpringApplication.run(StatsServer.class, args);
    }
}