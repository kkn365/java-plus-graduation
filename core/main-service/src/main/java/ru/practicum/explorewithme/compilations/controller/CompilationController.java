package ru.practicum.explorewithme.compilations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.compilations.dto.CompilationDto;
import ru.practicum.explorewithme.compilations.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilations.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilations.service.CompilationService;

import java.util.List;

import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    /**
     * Создание новой подборки событий.
     *
     * @param newCompilationDto DTO с данными новой подборки
     * @return Подборка событий в формате DTO (201 Created)
     */
    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> create(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Получен POST-запрос на создание подборки: {}", newCompilationDto.getTitle());
        CompilationDto created = compilationService.create(newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Удаление подборки по ID.
     *
     * @param compId Идентификатор подборки
     * @return Пустой ответ (204 No Content)
     */
    @DeleteMapping("/admin/compilations/{compId}")
    public ResponseEntity<Void> delete(@PathVariable Long compId) {
        log.info("Получен DELETE-запрос на удаление подборки с ID: {}", compId);
        compilationService.delete(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Обновление подборки по ID.
     *
     * @param compId         Идентификатор подборки
     * @param updateRequest  DTO с данными обновления
     * @return Обновлённая подборка (200 OK)
     */
    @PatchMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> update(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        log.info("Получен PATCH-запрос на обновление подборки с ID: {} данными: {}", compId, updateRequest);
        CompilationDto updated = compilationService.update(compId, updateRequest);
        return ResponseEntity.ok(updated);
    }

    /**
     * Получение списка подборок с фильтрацией и пагинацией.
     *
     * @param pinned Флаг закрепления (true/false)
     * @param from   Смещение для пагинации
     * @param size   Размер страницы
     * @return Список подборок (200 OK)
     */
    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getAll(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        log.info("Получен GET-запрос на получение подборок: pinned={}, from={}, size={}", pinned, from, size);
        PageRequest page = PageRequest.of(from / size, size);
        List<CompilationDto> compilations = compilationService.getAll(pinned, page);
        return ResponseEntity.ok(compilations);
    }

    /**
     * Получение подборки по ID.
     *
     * @param compId Идентификатор подборки
     * @return Подборка событий (200 OK)
     */
    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> getById(@PathVariable Long compId) {
        log.info("Получен GET-запрос на получение подборки с ID: {}", compId);
        CompilationDto compilation = compilationService.getById(compId);
        return ResponseEntity.ok(compilation);
    }
}