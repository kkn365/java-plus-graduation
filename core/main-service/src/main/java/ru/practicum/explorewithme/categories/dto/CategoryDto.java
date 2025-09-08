package ru.practicum.explorewithme.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @NotBlank(message = "Category name should not be blank.")
    @Size(min = 1, max = 50, message = "Category name should be between 1 and 50 characters.")
    private String name;
}