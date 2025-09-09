package ru.practicum.client.hit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.BaseClient;
import ru.practicum.dto.CreateHitDTO;


@Service
@Slf4j
public class HitClient extends BaseClient {
    public void hit(CreateHitDTO dto) {
        try {
            post("/hit", dto);
        } catch (Exception e) {
            log.info("HitClient::hit error: {}, {}, {}", e.getMessage(), e.getStackTrace(), e.getClass().getName());
        }
    }
}
