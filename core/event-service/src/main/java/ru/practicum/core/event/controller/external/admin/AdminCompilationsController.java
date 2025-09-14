package ru.practicum.core.event.controller.external.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.dto.compilations.NewCompilationDto;
import ru.practicum.core.event.dto.compilations.UpdateCompilationRequest;
import ru.practicum.core.event.service.api.CompilationService;

/**
 * Контроллер для работы с подборками событий в админской части.
 * <p>
 * Обрабатывает запросы на создание, удаление, обновление и получение подборок событий.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationsController {

    private final CompilationService compilationService;

    /**
     * Создаёт новую подборку событий.
     *
     * @param newCompilationDto DTO с данными новой подборки
     * @return ResponseEntity с DTO созданной подборки и статусом CREATED
     */
    @PostMapping
    public ResponseEntity<CompilationDto> create(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("POST /admin/compilations");

        CompilationDto created = compilationService.create(newCompilationDto);
        log.info("Подборка с ID={} успешно создана", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Удаляет подборку событий по её идентификатору.
     *
     * @param compId Идентификатор подборки
     * @return ResponseEntity с пустым телом и статусом NO_CONTENT
     */
    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> delete(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{}", compId);

        compilationService.delete(compId);
        log.info("Подборка с ID={} успешно удалена", compId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Обновляет подборку событий по её идентификатору.
     *
     * @param compId         Идентификатор подборки
     * @param updateRequest  DTO с данными для обновления подборки
     * @return ResponseEntity с DTO обновлённой подборки
     */
    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> update(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        log.info("PATCH /admin/compilations/{}", compId);

        CompilationDto updated = compilationService.update(compId, updateRequest);
        log.info("Подборка с ID={} успешно обновлена", compId);

        return ResponseEntity.ok(updated);
    }
}