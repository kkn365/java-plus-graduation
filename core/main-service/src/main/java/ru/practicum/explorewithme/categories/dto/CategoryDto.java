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
 * DTO для представления категории событий.
 * <p>
 * Используется при возврате данных клиенту. Содержит идентификатор и название категории.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    /**
     * Уникальный идентификатор категории.
     */
    private Long id;

    /**
     * Название категории.
     * <p>
     * Обязательное поле. Длина от 1 до 50 символов.
     */
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Название категории должно быть от 1 до 50 символов")
    private String name;
}