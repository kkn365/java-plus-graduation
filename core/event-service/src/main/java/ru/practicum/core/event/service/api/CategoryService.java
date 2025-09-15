package ru.practicum.core.event.service.api;

import jakarta.validation.ValidationException;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.dto.categories.NewCategoryDto;
import ru.practicum.core.event.model.Category;

import java.util.List;

/**
 * Интерфейс сервиса для работы с категориями событий.
 * <p>
 * Определяет методы для создания, обновления, удаления и получения категорий.
 */
public interface CategoryService {
    /**
     * Создаёт новую категорию.
     *
     * @param newCategoryDto данные новой категории
     * @return DTO созданной категории
     */
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    /**
     * Обновляет существующую категорию по её идентификатору.
     *
     * @param categoryId     идентификатор категории
     * @param categoryDto    данные обновлённой категории
     * @return DTO обновлённой категории
     */
    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    /**
     * Удаляет категорию по её идентификатору.
     *
     * @param categoryId идентификатор удаляемой категории
     */
    void deleteCategory(Long categoryId);

    /**
     * Получает категорию по её идентификатору.
     *
     * @param categoryId идентификатор категории
     * @return DTO категории
     */
    CategoryDto getCategory(Long categoryId);

    /**
     * Получает список категорий с возможностью пагинации.
     * <p>
     * Возвращает DTO-объекты категорий, ограниченные указанным диапазоном (from, size).
     *
     * @param from начальная позиция выборки (индекс)
     * @param size количество возвращаемых элементов
     * @return коллекция DTO категорий
     * @throws ValidationException если параметры пагинации некорректны (from < 0 или size <= 0)
     */
    List<CategoryDto> getCategories(Integer from, Integer size);

    /**
     * Возвращает сущность категории по её идентификатору.
     * <p>
     * Используется в основном для внутренней логики сервиса.
     *
     * @param categoryId идентификатор категории
     * @return сущность категории
     */
    Category getCategoryById(Long categoryId);
}