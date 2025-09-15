package ru.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.dto.categories.NewCategoryDto;
import ru.practicum.core.event.model.Category;

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
     * Преобразует модель категории в её DTO-представление.
     *
     * @param category модель категории
     * @return DTO категории
     */
    CategoryDto toDto(Category category);
}