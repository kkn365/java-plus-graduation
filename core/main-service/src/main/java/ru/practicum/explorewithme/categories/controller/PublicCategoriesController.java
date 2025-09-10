package ru.practicum.explorewithme.categories.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.service.CategoryService;

import java.util.Collection;

import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.explorewithme.util.PaginationConstants.DEFAULT_SIZE;

import lombok.extern.slf4j.Slf4j;

/**
 * Контроллер для получения информации о категориях событий.
 * <p>
 * Обрабатывает публичные запросы на получение списка категорий и конкретной категории.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoriesController {

    private final CategoryService categoryService;

    /**
     * Получает список всех категорий событий.
     *
     * @param from смещение (номер первой записи на странице)
     * @param size количество записей на странице
     * @return список DTO-объектов категорий (200 OK)
     */
    @GetMapping
    public ResponseEntity<Collection<CategoryDto>> getCategories(
            @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        log.info("GET /categories - Получен запрос на получение списка категорий. from: {}, size: {}", from, size);
        return ResponseEntity.ok(categoryService.getCategories(pageable));
    }

    /**
     * Получает конкретную категорию по её идентификатору.
     *
     * @param categoryId идентификатор категории
     * @return DTO-объект категории (200 OK)
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long categoryId) {
        log.info("GET /categories/{} - Получен запрос на получение категории", categoryId);
        return ResponseEntity.ok(categoryService.getCategory(categoryId));
    }
}