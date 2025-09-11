package ru.practicum.explorewithme.compilations.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.List;

/**
 * Сервис для управления подборками событий.
 * <p>
 * Предоставляет методы для создания, удаления, обновления и получения подборок.
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
     * @param pageable параметры пагинации (номер страницы, размер страницы, сортировка)
     * @return список DTO подборок
     */
    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    /**
     * Получает подборку по её идентификатору.
     *
     * @param compId идентификатор подборки
     * @return DTO подборки
     * @throws NotFoundException если подборка не найдена
     */
    CompilationDto getById(Long compId);
}