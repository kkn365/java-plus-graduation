package ru.practicum.core.event.dto.compilations;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * DTO для обновления подборки событий.
 * <p>
 * Содержит поля, которые могут быть изменены при обновлении подборки:
 * заголовок, флаг закрепления и список связанных событий.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    /**
     * Набор идентификаторов событий, включённых в подборку.
     * <p>
     * Может быть null — в этом случае список событий не изменяется.
     */
    @Schema(description = "Список идентификаторов событий", example = "[100, 200, 300]", nullable = true)
    private Set<Long> events;

    /**
     * Флаг закрепления подборки.
     * <p>
     * True — подборка отображается на главной странице, false — нет.
     * Может быть null — в этом случае флаг не изменяется.
     */
    @Schema(
            description = "Флаг закрепления подборки",
            example = "false",
            nullable = true)
    private Boolean pinned;

    /**
     * Заголовок подборки.
     * <p>
     * Длина от 1 до 50 символов. Обязательное поле.
     * Примеры допустимых значений: "События этой недели", "Концерты в Москве".
     */
    @Size(min = 1, max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    @Schema(
            description = "Заголовок подборки",
            example = "События этой недели",
            required = true,
            minLength = 1,
            maxLength = 50)
    private String title;
}