package ru.practicum.core.api.external;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.exception.RelatedDataDeleteException;
import ru.practicum.core.api.exception.StartAfterEndException;
import ru.practicum.core.api.exception.model.ApiError;

/**
 * Глобальный обработчик исключений для приложения.
 * <p>
 * Обрабатывает все исключения, возникающие в контроллерах, и возвращает соответствующие HTTP-ответы.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибку отсутствия объекта (HTTP 404).
     *
     * @param e Исключение, содержащее детали ошибки
     * @return Ответ с информацией об ошибке
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage(), e);
        return new ApiError(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден", e.getMessage());
    }

    /**
     * Обрабатывает ошибки валидации параметров запроса (HTTP 400).
     *
     * @param e Исключение, содержащее детали ошибки
     * @return Ответ с информацией об ошибке
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ValidationException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final Exception e) {
        log.error("ValidationException: {}", e.getMessage(), e);
        return new ApiError(HttpStatus.BAD_REQUEST, "Ошибка валидации входных данных", e.getMessage());
    }

    /**
     * Обрабатывает конфликты данных (HTTP 409).
     *
     * @param e Исключение, содержащее детали ошибки
     * @return Ответ с информацией об ошибке
     */
    @ExceptionHandler({
            DataAlreadyExistException.class,
            ConflictException.class,
            RelatedDataDeleteException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final Exception e) {
        log.error("ConflictException: {}", e.getMessage(), e);
        return new ApiError(HttpStatus.CONFLICT, "Конфликт данных", e.getMessage());
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

    /**
     * Обрабатывает внутренние ошибки сервера (HTTP 500).
     *
     * @param e Исключение, содержащее детали ошибки
     * @return Ответ с информацией об ошибке
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerError(final Exception e) {
        log.error("InternalServerError: {}", e.getMessage(), e);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка. Попробуйте позже.");
    }
}