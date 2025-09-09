package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.ewm.exception.model.StartAfterEndException;
import ru.practicum.ewm.stats.repository.HitsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final HitsRepository hitsRepository;

    public List<HitsStatDTO> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        checkStartAndEnd(start, end);

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return hitsRepository.findUniqueIpStats(start, end);
            } else {
                return hitsRepository.findAllStats(start, end);
            }
        } else {
            if (unique) {
                return hitsRepository.findUniqueIpStatsForUris(start, end, uris);
            } else {
                return hitsRepository.findAllStatsForUris(start, end, uris);
            }
        }
    }

    private void checkStartAndEnd(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new StartAfterEndException("Начало не должно быть после конца временного промежутка или совпадать с ним");
        }
    }
}