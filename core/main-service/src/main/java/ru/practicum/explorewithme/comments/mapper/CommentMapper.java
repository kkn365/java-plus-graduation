package ru.practicum.explorewithme.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.model.Comment;

import java.util.List;

/**
 * Mapper для преобразования между сущностью {@link Comment} и её DTO-представлением {@link CommentDto}.
 * <p>
 * Использует MapStruct для автоматической генерации логики преобразования.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    /**
     * Преобразует сущность комментария в DTO.
     * <p>
     * Включает дополнительные поля: eventId и authorId для удобства обработки на клиентской стороне.
     *
     * @param comment сущность комментария
     * @return DTO-представление комментария
     */
    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "authorId", source = "comment.author.id")
    CommentDto toDto(Comment comment);

    /**
     * Преобразует список сущностей комментариев в список DTO.
     *
     * @param comments список сущностей комментариев
     * @return список DTO-представлений комментариев
     */
    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "authorId", source = "comment.author.id")
    List<CommentDto> toDto(List<Comment> comments);
}