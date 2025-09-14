package ru.practicum.core.event.controller.external.pub;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.service.api.CategoryService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.*;

/**
 * Контроллер для публичного доступа к категориям событий.
 * <p>
 * Обрабатывает GET-запросы на получение списка категорий и отдельной категории.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoriesController {

    private final CategoryService categoryService;

    /**
     * Возвращает список категорий с возможностью пагинации.
     *
     * @param from Начальная позиция (смещение) для пагинации
     * @param size Количество элементов на странице
     * @return ResponseEntity со списком DTO категорий
     */
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
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        log.info("GET /categories/{}", categoryId);
        CategoryDto categoryDto = categoryService.getCategory(categoryId);
        log.info("Возвращена категория с ID={}", categoryDto.getId());
        return ResponseEntity.ok(categoryDto);
    }
}