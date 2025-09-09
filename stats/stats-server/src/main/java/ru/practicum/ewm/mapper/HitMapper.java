package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.CreateHitDTO;
import ru.practicum.ewm.stats.model.Hit;

@Mapper(componentModel = "spring")
public interface HitMapper {
    @Mapping(target = "id", ignore = true)
    Hit mapToHit(CreateHitDTO createHitDTO);
}
