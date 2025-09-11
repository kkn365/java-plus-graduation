package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.model.ApiError;
import ru.practicum.ewm.exception.model.StartAfterEndException;

/**
 * Глобальный обработчик исключений для приложения.
 * <p>
 * Централизованно обрабатывает исключения, возвращая структурированные ошибки в формате {@link ApiError}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает необработанные исключения.
     * Возвращает статус 500 и детализированное сообщение об ошибке.
     *
     * @param e исключение
     * @return объект ошибки
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerError(final Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                e.getMessage()
        );
    }

    /**
     * Обрабатывает отсутствие обязательного параметра в запросе.
     * Возвращает статус 400 и описание проблемы.
     *
     * @param e исключение
     * @return объект ошибки
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParam(final MissingServletRequestParameterException e) {
        log.warn("Отсутствует обязательный параметр: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Отсутствующий параметр",
                String.format("Параметр '%s' обязателен", e.getParameterName())
        );
    }

    /**
     * Обрабатывает ошибку, когда начальная дата позже конечной.
     * Возвращает статус 400 и понятное сообщение.
     *
     * @param e исключение
     * @return объект ошибки
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleStartAfterEnd(final StartAfterEndException e) {
        log.warn("Ошибка временного диапазона: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Некорректный временной диапазон",
                "Дата начала не может быть позже даты окончания"
        );
    }
}