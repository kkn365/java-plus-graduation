package ru.practicum.core.event.controller.external.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin: Подборки", description = "Операции для управления подборками событий (администратор)")
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
    @Operation(summary = "Создать новую подборку событий",
            description = "Пока не требует прав администратора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Подборка успешно создана",
                    content = @Content(schema = @Schema(implementation = CompilationDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
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
    @Operation(summary = "Удалить подборку событий",
            description = "Удаляет подборку по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Подборка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Подборка с указанным ID не найдена"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
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
    @Operation(summary = "Обновить подборку событий",
            description = "Обновляет подборку по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подборка успешно обновлена",
                    content = @Content(schema = @Schema(implementation = CompilationDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Подборка с указанным ID не найдена"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
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