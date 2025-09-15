package ru.practicum.core.event.service.api;

import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.dto.compilations.NewCompilationDto;
import ru.practicum.core.event.dto.compilations.UpdateCompilationRequest;
import ru.practicum.core.api.exception.NotFoundException;

import java.util.List;

/**
 * Интерфейс сервиса для работы с подборками событий.
 * <p>
 * Определяет методы для создания, обновления, поиска и управления подборками событиями.
 */
public interface CompilationService {
    /**
     * Создаёт новую подборку событий.
     *
     * @param dto данные для создания подборки
     * @return DTO созданной подборки
     */
    CompilationDto create(NewCompilationDto dto);

    /**
     * Удаляет подборку по её идентификатору.
     *
     * @param compId идентификатор подборки
     */
    void delete(Long compId);

    /**
     * Обновляет подборку событий.
     *
     * @param compId идентификатор подборки
     * @param dto    данные для обновления подборки
     * @return DTO обновлённой подборки
     */
    CompilationDto update(Long compId, UpdateCompilationRequest dto);

    /**
     * Получает список подборок с фильтрацией по закреплению и пагинацией.
     *
     * @param pinned флаг закрепления (true — только закреплённые)
     * @param from   Начальная позиция (смещение) для пагинации. Должно быть &gt;= 0.
     * @param size   Количество событий на странице. Должно быть &gt; 0.
     * @return список DTO подборок
     */
    List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size);

    /**
     * Получает подборку по её идентификатору.
     *
     * @param compId идентификатор подборки
     * @return DTO подборки
     * @throws NotFoundException если подборка не найдена
     */
    CompilationDto getById(Long compId);
}