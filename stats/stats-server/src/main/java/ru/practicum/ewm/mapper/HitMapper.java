package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.ewm.model.Hit;

/**
 * Mapper для преобразования между DTO {@link CreateHitDTO} и сущностью {@link Hit}.
 * <p>
 * Используется MapStruct для автоматической генерации логики преобразования.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {
    /**
     * Преобразует DTO {@link CreateHitDTO} в модель {@link Hit}.
     * <p>
     * Поле `id` игнорируется, так как оно генерируется при сохранении в БД.
     *
     * @param createHitDTO данные для создания хита
     * @return модель хита
     */
    @Mapping(target = "id", ignore = true)
    Hit mapToHit(CreateHitDTO createHitDTO);
}