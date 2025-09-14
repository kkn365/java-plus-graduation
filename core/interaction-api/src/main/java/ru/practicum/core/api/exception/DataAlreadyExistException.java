package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * Исключение, выбрасываемое при попытке создать объект, который уже существует в системе.
 * <p>
 * Используется для обработки ситуаций, когда уникальность данных нарушена.
 */
public class DataAlreadyExistException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7092856392954898635L;

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текстовое сообщение
     */
    public DataAlreadyExistException(String message) {
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
    public DataAlreadyExistException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    /**
     * Конструктор с указанием причины исключения.
     * <p>
     * Позволяет оборачивать другие исключения.
     *
     * @param cause исходное исключение
     */
    public DataAlreadyExistException(Throwable cause) {
        super(cause);
    }

    /**
     * Конструктор с сообщением и причиной исключения.
     *
     * @param message текстовое сообщение
     * @param cause   исходное исключение
     */
    public DataAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Возвращает поставщика исключения с параметризированным сообщением.
     *
     * @param message шаблон сообщения
     * @param args    аргументы для подстановки в шаблон
     * @return поставщик исключения
     */
    public static Supplier<DataAlreadyExistException> dataAlreadyExistException(String message, Object... args) {
        return () -> new DataAlreadyExistException(message, args);
    }
}