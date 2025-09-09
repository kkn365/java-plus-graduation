package ru.practicum.explorewithme.categories.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.categories.dto.CategoryDto;
import ru.practicum.explorewithme.categories.dto.NewCategoryDto;
import ru.practicum.explorewithme.categories.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toModel(NewCategoryDto newCategoryDto);

    Category toModel(CategoryDto categoryDto);

    CategoryDto toDto(Category category);
}
