package ru.practicum.explorewithme.comments.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.explorewithme.comments.model.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdminCommentParams {

    private List<Long> comments;
    private String text;
    private List<Long> events;
    private List<Long> authors;
    private List<CommentStatus> status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDateEnd;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedDateEnd;

    private Integer from = 0;
    private Integer size = 10;
}
