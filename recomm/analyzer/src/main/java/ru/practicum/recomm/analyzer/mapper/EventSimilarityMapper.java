package ru.practicum.recomm.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.recomm.analyzer.model.EventSimilarity;
import ru.practicum.recommendations.avro.EventSimilarityAvro;

/**
 * Mapper для преобразования объектов между форматами Avro и моделью JPA.
 * <p>
 * Используется MapStruct для автоматической генерации кода преобразования.
 * Класс предназначен для перевода данных из Avro-объекта в сущность EventSimilarity.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventSimilarityMapper {

    /**
     * Преобразует объект {@link EventSimilarityAvro} в сущность {@link EventSimilarity}.
     * <p>
     * Поле id игнорируется, так как оно генерируется базой данных при сохранении.
     *
     * @param avro Avro-объект, содержащий данные о схожести мероприятий
     * @return Сущность EventSimilarity, построенная на основе входных данных
     */
    @Mapping(target = "id", ignore = true)
    EventSimilarity toEventSimilarity(EventSimilarityAvro avro);
}