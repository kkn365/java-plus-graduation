package ru.practicum.ewm.service;

import ru.practicum.dto.HitsStatDTO;
import ru.practicum.ewm.exception.model.StartAfterEndException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Интерфейс сервиса для получения статистики просмотров (hits).
 * <p>
 * Предоставляет методы для получения агрегированных данных о количестве просмотров событий
 * в заданном временном диапазоне с возможностью фильтрации по URI и учету уникальных IP.
 */
public interface StatsService {

    /**
     * Получает статистику просмотров событий за указанный период времени.
     *
     * @param start  начальная дата диапазона (включительно)
     * @param end    конечная дата диапазона (включительно)
     * @param uris   список URI для фильтрации (опционально)
     * @param unique флаг, указывающий, нужно ли учитывать уникальные IP-адреса
     * @return список DTO со статистикой просмотров
     * @throws StartAfterEndException если начальная дата позже или равна конечной
     */
    List<HitsStatDTO> getStats(LocalDateTime start,
                               LocalDateTime end,
                               List<String> uris,
                               boolean unique) throws StartAfterEndException;
}