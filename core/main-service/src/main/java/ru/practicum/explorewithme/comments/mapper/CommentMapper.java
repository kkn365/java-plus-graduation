package ru.practicum.explorewithme.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "authorId", source = "comment.author.id")
    CommentDto toDto(Comment comment);

    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "authorId", source = "comment.author.id")
    List<CommentDto> toDto(List<Comment> comments);
}