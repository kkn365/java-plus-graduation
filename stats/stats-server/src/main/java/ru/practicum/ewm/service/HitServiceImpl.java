package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.repository.HitsRepository;

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
    @Override
    @Transactional
    public void createHit(CreateHitDTO dto) {
        Hit newHit = hitMapper.mapToHit(dto);
        log.info("Создан новый хит: {}", dto);
        hitsRepository.save(newHit);
    }
}