package ru.practicum.core.event.controller.external.pub;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.*;

import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.service.api.CategoryService;

/**
 * Контроллер для публичного доступа к категориям событий.
 * <p>
 * Обрабатывает GET-запросы на получение списка категорий и отдельной категории.
 */
@Tag(name = "Public: Категории", description = "Операции для получения информации о категориях событий (публичный доступ)")
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoriesController {

    private final CategoryService categoryService;

    /**
     * Возвращает список категорий с возможностью пагинации.
     *
     * @param from Начальная позиция (смещение) для пагинации
     * @param size Количество элементов на странице
     * @return ResponseEntity со списком DTO категорий
     */
    @Operation(summary = "Получить список категорий",
            description = "Возвращает список всех категорий событий. Поддерживает пагинацию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /categories?from={}&size={}", from, size);
        List<CategoryDto> categories = categoryService.getCategories(from, size);
        log.info("Возвращено {} категорий", categories.size());
        return ResponseEntity.ok(categories);
    }

    /**
     * Возвращает информацию о конкретной категории по её ID.
     *
     * @param categoryId Идентификатор категории
     * @return ResponseEntity с DTO категории
     */
    @Operation(summary = "Получить категорию по ID",
            description = "Возвращает информацию о категории событий по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о категории успешно получена",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Категория с указанным ID не найдена"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        log.info("GET /categories/{}", categoryId);
        CategoryDto categoryDto = categoryService.getCategory(categoryId);
        log.info("Возвращена категория с ID={}", categoryDto.getId());
        return ResponseEntity.ok(categoryDto);
    }
}