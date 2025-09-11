package ru.practicum.client;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.dto.HitsStatDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Feign-клиент для взаимодействия с сервисом статистики (stats-service).
 * <p>
 * Обеспечивает методы для получения статистики по URI и отправки новых записей о просмотрах.
 */
@FeignClient(name = "stats-server")
public interface StatsClient {

    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
    @GetMapping("/stats")
    ResponseEntity<List<HitsStatDTO>> getStats(
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) throws FeignException;

    /**
     * Отправляет новую запись о просмотре (hit).
     *
     * @param createHitDTO данные для создания hit
     * @return ответ без тела (201 Created)
     * @throws FeignException при ошибке запроса к микросервису
     */
    @PostMapping("/hit")
    ResponseEntity<Void> createHit(@Valid @RequestBody CreateHitDTO createHitDTO) throws FeignException;
}