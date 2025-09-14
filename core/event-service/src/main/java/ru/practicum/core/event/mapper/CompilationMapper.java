package ru.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.model.Compilation;

/**
 * Маппер для преобразования между сущностью {@link Compilation} и её DTO-представлением.
 * <p>
 * Использует MapStruct для автоматической генерации логики преобразования.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {EventMapper.class})
public interface CompilationMapper {

    /**
     * Преобразует сущность подборки событий в её DTO-представление.
     *
     * @param compilation Сущность подборки событий
     * @return DTO-объект подборки событий
     */
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "title", target = "title"),
            @Mapping(source = "pinned", target = "pinned"),
            @Mapping(source = "events", target = "events")
    })
    CompilationDto toDto(Compilation compilation);
}