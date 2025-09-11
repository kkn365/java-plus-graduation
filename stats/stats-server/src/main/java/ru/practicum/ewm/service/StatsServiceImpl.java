package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.ewm.exception.model.StartAfterEndException;
import ru.practicum.ewm.repository.HitsRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса для получения статистики по просмотрам (hits).
 * <p>
 * Обрабатывает запросы на получение статистики с учётом временных рамок, URI и флага уникальности IP.
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final HitsRepository hitsRepository;

    /**
     * Получает статистику просмотров за указанный период.
     *
     * @param start  начальная дата диапазона (включительно)
     * @param end    конечная дата диапазона (включительно)
     * @param uris   список URI для фильтрации (опционально)
     * @param unique флаг, указывающий, нужно ли учитывать уникальные IP-адреса
     * @return список DTO статистики
     * @throws StartAfterEndException если start > end
     */
    public List<HitsStatDTO> getStats(LocalDateTime start,
                                      LocalDateTime end,
                                      List<String> uris,
                                      boolean unique) {
        validateDateRange(start, end);
        boolean hasUris = uris != null && !uris.isEmpty();

        if (hasUris) {
            return unique
                    ? hitsRepository.findUniqueIpStatsForUris(start, end, uris)
                    : hitsRepository.findAllStatsForUris(start, end, uris);
        } else {
            return unique
                    ? hitsRepository.findUniqueIpStats(start, end)
                    : hitsRepository.findAllStats(start, end);
        }
    }

    /**
     * Проверяет корректность временного диапазона.
     * Начальная дата не должна быть позже или равна конечной.
     *
     * @param start начальная дата
     * @param end   конечная дата
     * @throws StartAfterEndException если start >= end
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new StartAfterEndException("Начальная дата должна быть строго меньше конечной");
        }
    }
}