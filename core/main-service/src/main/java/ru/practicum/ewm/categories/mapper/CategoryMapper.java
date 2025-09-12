package ru.practicum.ewm.categories.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.categories.model.Category;

/**
 * Маппер для преобразования между сущностью {@link Category} и её DTO-представлениями.
 * <p>
 * Использует MapStruct для автоматической генерации логики преобразования.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    /**
     * Преобразует DTO нового объекта категории в модель категории.
     * <p>
     * Идентификатор игнорируется, так как он генерируется автоматически.
     *
     * @param newCategoryDto данные новой категории
     * @return модель категории
     */
    @Mapping(target = "id", ignore = true)
    Category toModel(NewCategoryDto newCategoryDto);

    /**
     * Преобразует DTO существующей категории в модель категории.
     *
     * @param categoryDto данные категории
     * @return модель категории
     */
    Category toModel(CategoryDto categoryDto);

    /**
     * Преобразует модель категории в её DTO-представление.
     *
     * @param category модель категории
     * @return DTO категории
     */
    CategoryDto toDto(Category category);
}