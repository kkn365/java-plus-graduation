package ru.practicum.explorewithme.comments.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.comments.model.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime publishedDate;
    private CommentStatus status;
}
