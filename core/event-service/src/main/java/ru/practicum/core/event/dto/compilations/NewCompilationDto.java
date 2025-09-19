package ru.practicum.core.event.dto.compilations;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Список идентификаторов событий для подборки", example = "[100, 200, 300]")
    private Set<Long> events;

    /**
     * Флаг закрепления подборки.
     * <p>
     * True — подборка будет отображаться на главной странице.
     * По умолчанию: false.
     */
    @Schema(description = "Флаг закрепления (true — отображается на главной странице)", example = "false")
    @Builder.Default
    private Boolean pinned = false;

    /**
     * Заголовок подборки.
     * <p>
     * Обязательное поле. Длина от 1 до 50 символов.
     * Примеры: "События этой недели", "Концерты в Москве".
     */
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 1, max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    @Schema(
            description = "Заголовок подборки",
            example = "События этой недели",
            required = true,
            minLength = 1,
            maxLength = 50)
    private String title;
}