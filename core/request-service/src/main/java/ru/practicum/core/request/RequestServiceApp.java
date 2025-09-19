package ru.practicum.core.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = {
        "ru.practicum.core.api.internal.user.client",
        "ru.practicum.core.api.internal.request.client",
        "ru.practicum.core.api.internal.event.client",
        "ru.practicum.core.api"
})
@ComponentScan(basePackages = {
        "ru.practicum.core.request",
        "ru.practicum.core.api",
        "ru.practicum.recomm.client"
})
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}
