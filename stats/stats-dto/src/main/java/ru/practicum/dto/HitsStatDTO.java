package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.dto.validator.ValidEndpoint;

/**
 * DTO для представления статистики просмотров (hits).
 * <p>
 * Содержит данные о приложении, URI и количестве просмотров.
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HitsStatDTO {

    /**
     * Название приложения, к которому относится статистика.
     * Обязательное поле, не может быть пустым.
     */
    @NotBlank(message = "Название приложения не может быть пустым")
    private String app;

    /**
     * URI, по которому был совершён запрос.
     * Обязательное поле, должно соответствовать правилам валидации эндпоинта.
     */
    @NotNull(message = "URI не может быть null")
    @ValidEndpoint
    private String uri;

    /**
     * Количество просмотров (или уникальных IP-адресов) для данного URI.
     * Обязательное поле.
     */
    @NotNull(message = "Количество просмотров не может быть null")
    private Long hits;
}