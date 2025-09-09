package ru.practicum.explorewithme.comments.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.explorewithme.comments.model.CommentStatus;

@Data
public class CommentPatchDto {

    @NotBlank
    private CommentStatus status;
}
