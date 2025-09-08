package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MainServer {
    public static void main(String[] args) {
        SpringApplication.run(MainServer.class, args);
    }
}