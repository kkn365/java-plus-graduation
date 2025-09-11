package ru.practicum.ewm.service;

import ru.practicum.dto.CreateHitDTO;

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