package ru.practicum.stats.server.service;

import ru.practicum.stats.dto.CreateHitDTO;

/**
 * Интерфейс сервиса для работы с хитами (просмотрами).
 * <p>
 * Предоставляет методы для создания записей о просмотрах событий.
 */
public interface HitService {
    /**
     * Создаёт новую запись о просмотре события.
     *
     * @param createHitDTO данные для создания хита
     */
    void createHit(CreateHitDTO createHitDTO);
}