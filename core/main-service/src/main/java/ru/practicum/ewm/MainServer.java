package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = {"ru.practicum.core.api", "ru.practicum.client"})
@ComponentScan(basePackages = {"ru.practicum.ewm", "ru.practicum.core.api"})
public class MainServer {
    public static void main(String[] args) {
        SpringApplication.run(MainServer.class, args);
    }
}