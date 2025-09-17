package ru.practicum.core.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = {"ru.practicum.core.api", "ru.practicum.stats.client"})
@ComponentScan(basePackages = {
        "ru.practicum.core.event",
        "ru.practicum.core.api",
        "ru.practicum.recomm.client"
})
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}