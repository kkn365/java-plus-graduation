package ru.practicum.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.core.event.repository.EventRepository;
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

    private static final String CATEGORY_EXISTS_ERROR_MESSAGE = "Категория с именем=%s уже существует в базе данных";
    private static final String DELETION_ERROR_MESSAGE = "Категория с ID=%d связана с другими сущностями и не может быть удалена";
    private static final String GET_ERROR_MESSAGE = "Категория с ID=%d не найдена в базе данных";

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Метод создания новой категории.
     * <p>
     * Выполняет проверку уникальности имени категории в базе данных. Если категория с таким именем уже существует,
     * выбрасывается исключение {@link DataAlreadyExistException}. В противном случае создаётся новая категория,
     * сохраняется в репозиторий, и логируется её идентификатор.
     *
     * @param newCategoryDto объект DTO с данными новой категории (имя категории)
     * @return объект DTO созданной категории
     * @throws DataAlreadyExistException если категория с указанным именем уже существует
     */
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        checkCategoryExistenceByNameOrThrow(newCategoryDto.getName());

        Category newCategory = categoryMapper.toModel(newCategoryDto);
        Category createdCategory = categoryRepository.save(newCategory);

        log.info("Создана новая категория с ID={}", createdCategory.getId());
        return categoryMapper.toDto(createdCategory);
    }

    /**
     * Метод обновления информации о категории по её идентификатору.
     * <p>
     * Обновляет имя категории, если оно отличается от текущего. Если новое имя совпадает с текущим,
     * возвращается DTO текущей категории без изменений. В противном случае изменения сохраняются в репозиторий.
     *
     * @param categoryId  идентификатор категории, которую необходимо обновить
     * @param categoryDto объект DTO с новыми данными категории (в данном случае — новое имя)
     * @return объект DTO обновлённой категории
     * @throws NotFoundException если категория с указанным ID не найдена
     */
    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        // Проверяем, что категория с указанным ID существует
        Category currentCategory = getCategoryById(categoryId);
        // Проверяем, что новое имя категории не совпадает с текущим
        if (currentCategory.getName().equals(categoryDto.getName())) {
            // Если имена совпадают, возвращаем DTO текущей категории
            return categoryMapper.toDto(currentCategory);
        }
        // Обновляем название категории
        currentCategory.setName(categoryDto.getName());
        // Сохраняем обновлённую категорию в репозиторий
        Category updatedCategory = categoryRepository.save(currentCategory);

        log.info("Название категории с ID={} изменено на: {}", updatedCategory.getId(), updatedCategory.getName());
        return categoryMapper.toDto(updatedCategory);
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
        // Если категория не найдена, генерируется исключение NotFoundException
        if (categoryRepository.existsById(categoryId)) {
            // Проверяем, связана ли категория с другими сущностями
            if (eventRepository.existsByCategoryId(categoryId)) {
                final String error = String.format(DELETION_ERROR_MESSAGE, categoryId);
                log.warn(error);
                throw new RelatedDataDeleteException(error);
            }
            // Удаляем категорию
            categoryRepository.deleteById(categoryId);
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
     * <p>
     * Возвращает список DTO категорий, ограниченный по количеству и смещённый на заданное количество записей.
     *
     * @param from количество пропускаемых записей (смещение)
     * @param size количество возвращаемых записей на странице
     * @return список DTO категорий
     */
    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

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

    /**
     * Проверяет, существует ли категория с указанным именем в базе данных.
     * <p>
     * Если категория с таким именем уже существует, генерируется исключение {@link DataAlreadyExistException}.
     *
     * @param name имя категории, которую необходимо проверить
     * @throws DataAlreadyExistException если категория с указанным именем уже существует
     */
    private void checkCategoryExistenceByNameOrThrow(String name) {
        if (categoryRepository.existsByName(name)) {
            final String error = String.format(CATEGORY_EXISTS_ERROR_MESSAGE, name);
            log.warn(error);
            throw new DataAlreadyExistException(error);
        }
    }
}