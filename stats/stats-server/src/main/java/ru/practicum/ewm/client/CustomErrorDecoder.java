package ru.practicum.ewm.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Пользовательский декодер ошибок для Feign-клиентов.
 * <p>
 * Преобразует ответы с HTTP-ошибками в исключения, добавляя контекст и детали ошибки.
 */
@Component
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int rawStatus = response.status();
        HttpStatus httpStatus = HttpStatus.resolve(rawStatus);

        // Логируем основные параметры ошибки
        log.error("Feign error: method={}, status={}, reason={}",
                methodKey,
                rawStatus,
                response.reason());

        // Получаем тело ответа для более подробного логирования
        String responseBody = getResponseBody(response);

        // Обработка известных статусов
        if (httpStatus != null) {
            return switch (httpStatus) {
                case BAD_REQUEST -> new BadRequestException(
                        "Bad Request [status=400, method=" + methodKey + ", response=" + responseBody + "]");
                case INTERNAL_SERVER_ERROR -> new InternalServerErrorException(
                        "Internal Server Error [status=500, method=" + methodKey + ", response=" + responseBody + "]");
                default -> {
                    // Для других статусов используем дефолтный декодер, но добавляем контекст
                    Exception defaultException = defaultErrorDecoder.decode(methodKey, response);
                    log.warn("Unhandled status {}: {}", httpStatus, responseBody);
                    yield defaultException;
                }
            };
        } else {
            // Неизвестный HTTP-статус — создаём обобщённое исключение
            log.warn("Unknown HTTP status code: {}", rawStatus);
            return new RuntimeException("Unknown HTTP status [" + rawStatus + "]");
        }
    }

    /**
     * Вспомогательный метод для получения тела ответа из Feign-ответа.
     *
     * @param response Feign-ответ
     * @return Содержимое тела ответа в виде строки или пустая строка при ошибке
     */
    private String getResponseBody(Response response) {
        try {
            byte[] bodyBytes = response.body().asInputStream().readAllBytes();
            return new String(bodyBytes, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            log.warn("Failed to read response body", e);
            return "";
        }
    }
}