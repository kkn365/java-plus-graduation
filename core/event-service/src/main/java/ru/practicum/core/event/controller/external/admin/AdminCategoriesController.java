package ru.practicum.core.event.controller.external.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.dto.categories.NewCategoryDto;
import ru.practicum.core.event.service.api.CategoryService;

/**
 * Контроллер для управления категориями событий.
 * <p>
 * Обрабатывает запросы на создание, удаление и обновление категорий.
 * Доступен только для администраторов.
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoriesController {

    private final CategoryService categoryService;

    /**
     * Создаёт новую категорию.
     *
     * @param newCategoryDto данные новой категории
     * @return DTO созданной категории (201 Created)
     */
    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("POST /admin/categories - Получен запрос на создание новой категории: {}", newCategoryDto);
        CategoryDto created = categoryService.createCategory(newCategoryDto);
        log.info("Категория с ID={} успешно создана", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Удаляет категорию по её идентификатору.
     *
     * @param categoryId идентификатор категории
     * @return пустой ответ (204 No Content)
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        log.info("DELETE /admin/categories/{} - Получен запрос на удаление категории", categoryId);
        categoryService.deleteCategory(categoryId);
        log.info("Категория с ID={} успешно удалена", categoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновляет категорию по её идентификатору.
     *
     * @param categoryId   идентификатор категории
     * @param categoryDto  DTO с обновлёнными данными
     * @return DTO обновлённой категории (200 OK)
     */
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long categoryId,
                                              @RequestBody @Valid CategoryDto categoryDto) {
        log.info("PATCH /admin/categories/{} - Получен запрос на обновление категории: {}", categoryId, categoryDto);
        CategoryDto updated = categoryService.updateCategory(categoryId, categoryDto);
        log.info("Категория с ID={} успешно обновлена", categoryId);
        return ResponseEntity.ok(updated);
    }
}