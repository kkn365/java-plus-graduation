package ru.practicum.explorewithme.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * DTO для создания новой подборки событий.
 * <p>
 * Содержит обязательные и необязательные поля, необходимые для инициализации подборки.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {

    /**
     * Набор идентификаторов событий, которые будут включены в подборку.
     * <p>
     * Может быть пустым, если события добавятся позже.
     */
    private Set<Long> events;

    /**
     * Флаг закрепления подборки.
     * <p>
     * True — подборка будет отображаться на главной странице.
     * По умолчанию: false.
     */
    private Boolean pinned = false;

    /**
     * Заголовок подборки.
     * <p>
     * Обязательное поле. Длина от 1 до 50 символов.
     * Примеры: "События этой недели", "Концерты в Москве".
     */
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 1, max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    private String title;
}