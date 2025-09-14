package ru.practicum.core.event.controller.external.pub;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.service.api.CompilationService;

import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.*;

/**
 * Контроллер для обработки публичных запросов, связанных с подборками событий.
 * <p>
 * Предоставляет методы для получения списка подборок и информации о конкретной подборке.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationsController {

    /**
     * Сервис для работы с подборками событий.
     */
    private final CompilationService compilationService;

    /**
     * Метод для получения списка подборок событий.
     * <p>
     * Поддерживает фильтрацию по флагу закрепления (pinned) и пагинацию.
     *
     * @param pinned флаг закрепления. Если true — возвращаются только закреплённые подборки.
     * @param from   начальная позиция для пагинации (количество пропускаемых записей)
     * @param size   количество возвращаемых записей на странице
     * @return ResponseEntity с списком DTO подборок событий
     */
    @GetMapping
    public ResponseEntity<List<CompilationDto>> getAll(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /compilations?pinned={}&from={}&size={}", pinned, from, size);
        List<CompilationDto> compilations = compilationService.getAll(pinned, from, size);
        log.info("Возвращено {} подборок", compilations.size());
        return ResponseEntity.ok(compilations);
    }

    /**
     * Метод для получения информации о конкретной подборке событий по её идентификатору.
     *
     * @param compId идентификатор подборки
     * @return ResponseEntity с DTO подборки событий
     * @throws NotFoundException если подборка с указанным ID не найдена
     */
    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getById(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        CompilationDto compilation = compilationService.getById(compId);
        log.info("Подборка с ID={} успешно получена", compId);
        return ResponseEntity.ok(compilation);
    }
}