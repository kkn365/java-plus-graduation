package ru.practicum.ewm.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.ewm.service.HitService;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController implements StatsClient {

    private final StatsService statsService;
    private final HitService hitService;

    /**
     * Получает статистику за определённый период времени.
     *
     * @param start  начальная дата диапазона (включительно)
     * @param end    конечная дата диапазона (включительно)
     * @param uris   список URI для фильтрации (опционально)
     * @param unique флаг, указывающий, нужно ли учитывать уникальных пользователей
     * @return коллекция DTO статистики (HitsStatDTO)
     * @throws FeignException при ошибке запроса к микросервису
     */
    @Override
    public ResponseEntity<List<HitsStatDTO>> getStats(LocalDateTime start,
                                                            LocalDateTime end,
                                                            List<String> uris,
                                                            boolean unique) throws FeignException {
        log.info("GET /stats?start={}&end={}&uris={}&unique={} - Получен запрос на получение статистики",
                start, end, uris, unique);
        return ResponseEntity.ok().body(statsService.getStats(start, end, uris, unique));
    }

    /**
     * Отправляет новую запись о просмотре (hit).
     *
     * @param createHitDTO данные для создания hit
     * @return ответ без тела (201 Created)
     * @throws FeignException при ошибке запроса к микросервису
     */
    @Override
    public ResponseEntity<Void> createHit(CreateHitDTO createHitDTO) throws FeignException {
        log.info("POST /hit - Получен запрос на создание hit: {}", createHitDTO);
        hitService.createHit(createHitDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
