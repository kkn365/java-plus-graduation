package ru.practicum.explorewithme.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO для создания новой категории событий.
 * <p>
 * Содержит только обязательное поле — название категории.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "name")
@ToString(of = {"name"})
public class NewCategoryDto {
    /**
     * Название категории.
     * <p>
     * Обязательное поле. Длина от 1 до 50 символов.
     */
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Название категории должно быть от 1 до 50 символов")
    private String name;
}
