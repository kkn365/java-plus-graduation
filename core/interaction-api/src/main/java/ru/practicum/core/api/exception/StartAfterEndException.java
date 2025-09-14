package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Исключение, выбрасываемое при попытке установить начальное время позже конечного.
 * <p>
 * Используется для обработки логических ошибок валидации временных диапазонов.
 */
public class StartAfterEndException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7092856392954898635L;

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текстовое сообщение об ошибке
     */
    public StartAfterEndException(String message) {
        super(message);
    }

    /**
     * Конструктор с параметризированным сообщением.
     * <p>
     * Позволяет использовать MessageFormat для подстановки аргументов в сообщение.
     *
     * @param message шаблон сообщения
     * @param args    аргументы для подстановки в шаблон
     */
    public StartAfterEndException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    /**
     * Конструктор с указанием причины исключения.
     * <p>
     * Позволяет оборачивать другие исключения.
     *
     * @param cause исходное исключение
     */
    public StartAfterEndException(Throwable cause) {
        super(cause);
    }

    /**
     * Конструктор с сообщением и причиной исключения.
     *
     * @param message текстовое сообщение
     * @param cause   исходное исключение
     */
    public StartAfterEndException(String message, Throwable cause) {
        super(message, cause);
    }
}