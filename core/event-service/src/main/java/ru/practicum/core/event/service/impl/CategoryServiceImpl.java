package ru.practicum.core.event.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.dto.categories.NewCategoryDto;
import ru.practicum.core.event.mapper.CategoryMapper;
import ru.practicum.core.event.model.Category;
import ru.practicum.core.event.repository.CategoryRepository;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.exception.RelatedDataDeleteException;
import ru.practicum.core.event.service.api.CategoryService;

import java.util.List;

/**
 * Реализация сервиса для работы с категориями событий.
 * <p>
 * Класс предоставляет методы для создания, обновления, удаления и получения информации о категориях,
 * а также валидации входных данных и взаимодействия с репозиторием.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CREATION_ERROR_MESSAGE = "Категория с именем=%s уже существует в базе данных";
    private static final String DELETION_ERROR_MESSAGE = "Категория с ID=%d связана с другими сущностями и не может быть удалена";
    private static final String GET_ERROR_MESSAGE = "Категория с ID=%d не найдена в базе данных";
    private static final String PAGINATION_ERROR_MESSAGE = "Некорректные параметры пагинации";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Метод создания новой категории.
     *
     * @param newCategoryDto DTO с данными новой категории
     * @return DTO созданной категории
     * @throws DataAlreadyExistException если категория с таким именем уже существует
     */
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category newCategory = categoryMapper.toModel(newCategoryDto);
        try {
            Category createdCategory = categoryRepository.save(newCategory);
            return categoryMapper.toDto(createdCategory);
        } catch (DataIntegrityViolationException e) {
            final String error = String.format(CREATION_ERROR_MESSAGE, newCategoryDto.getName());
            log.warn(error);
            throw new DataAlreadyExistException(error);
        }
    }

    /**
     * Метод обновления информации о категории.
     *
     * @param categoryId   идентификатор категории, которую необходимо обновить
     * @param categoryDto  DTO с новыми данными категории
     * @return DTO обновлённой категории
     * @throws DataAlreadyExistException если категория с таким именем уже существует
     * @throws NotFoundException         если категория с указанным ID не найдена
     */
    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category currentCategory = getCategoryById(categoryId);
        currentCategory.setName(categoryDto.getName());
        try {
            Category updatedCategory = categoryRepository.save(currentCategory);
            return categoryMapper.toDto(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            final String error = String.format(CREATION_ERROR_MESSAGE, categoryDto.getName());
            log.warn(error);
            throw new DataAlreadyExistException(error);
        }
    }

    /**
     * Метод удаления категории по её идентификатору.
     *
     * @param categoryId идентификатор категории, которую необходимо удалить
     * @throws RelatedDataDeleteException если категория связана с другими сущностями и не может быть удалена
     * @throws NotFoundException          если категория с указанным ID не найдена
     */
    @Override
    public void deleteCategory(Long categoryId) {
        try {
            categoryRepository.deleteById(categoryId);
        } catch (DataIntegrityViolationException e) {
            final String error = String.format(DELETION_ERROR_MESSAGE, categoryId);
            log.warn(error);
            throw new RelatedDataDeleteException(error);
        }
    }

    /**
     * Метод получения информации о категории по её идентификатору.
     *
     * @param categoryId идентификатор категории
     * @return DTO категории
     * @throws NotFoundException если категория с указанным ID не найдена
     */
    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category currentCategory = getCategoryById(categoryId);
        return categoryMapper.toDto(currentCategory);
    }

    /**
     * Метод получения списка категорий с пагинацией.
     *
     * @param from количество пропускаемых записей (смещение)
     * @param size количество возвращаемых записей на странице
     * @return список DTO категорий
     * @throws ValidationException если параметры пагинации некорректны
     */
    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        // Проверяем параметры пагинации
        if (from < 0 || size <= 0) {
            throw new ValidationException(PAGINATION_ERROR_MESSAGE);
        }

        int page = from / size;

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    /**
     * Метод получения категории по её идентификатору.
     *
     * @param categoryId идентификатор категории
     * @return сущность категории
     * @throws NotFoundException если категория с указанным ID не найдена
     */
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format(GET_ERROR_MESSAGE, categoryId)));
    }
}