package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.CreateHitDTO;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.repository.HitsRepository;

/**
 * Реализация сервиса для работы с хитами (просмотрами).
 * <p>
 * Обрабатывает создание записей о просмотрах событий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitsRepository hitsRepository;
    private final HitMapper hitMapper;

    /**
     * Создаёт новую запись о просмотре события.
     *
     * @param dto данные для создания хита
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public void createHit(CreateHitDTO dto) {
        Hit newHit = hitMapper.mapToHit(dto);
        log.info("Создан новый хит: {}", dto);
        hitsRepository.save(newHit);
    }
}