package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.dto.validator.ValidEndpoint;
import ru.practicum.dto.validator.ValidIpAddress;

import java.time.LocalDateTime;

/**
 * DTO для создания новой записи просмотра (hit).
 * <p>
 * Содержит данные о приложении, URI, IP-адресе и времени запроса.
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateHitDTO {

    /**
     * Название приложения, которое было просмотрено.
     * Обязательное поле, не может быть пустым.
     */
    @NotBlank(message = "Название приложения не может быть пустым")
    private String app;

    /**
     * URI, который был посещён.
     * Обязательное поле, должно соответствовать правилам валидации эндпоинта.
     */
    @NotNull(message = "URI не может быть null")
    @ValidEndpoint
    private String uri;

    /**
     * IP-адрес пользователя, совершившего запрос.
     * Обязательное поле, должно соответствовать формату IPv4/IPv6.
     */
    @NotNull(message = "IP-адрес не может быть null")
    @ValidIpAddress
    private String ip;

    /**
     * Дата и время, когда произошёл просмотр.
     * Обязательное поле, формат: yyyy-MM-dd HH:mm:ss.
     */
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Дата и время не могут быть null")
    private LocalDateTime timestamp;
}