package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * Исключение, выбрасываемое при отсутствии запрашиваемых данных.
 * <p>
 * Используется для обработки ситуаций, когда объект не найден в хранилище.
 */
public class NotFoundException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -892134765987654321L;

    /**
     * Создаёт исключение без сообщения.
     */
    public NotFoundException() {
        super();
    }

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message текстовое сообщение
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанным сообщением и причиной.
     *
     * @param message текстовое сообщение
     * @param cause   исходное исключение
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Создаёт исключение с указанной причиной.
     *
     * @param cause исходное исключение
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Создаёт исключение с параметризированным сообщением.
     * <p>
     * Позволяет использовать MessageFormat для подстановки аргументов в сообщение.
     *
     * @param message шаблон сообщения
     * @param args    аргументы для подстановки в шаблон
     */
    public NotFoundException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    /**
     * Возвращает поставщика исключения с параметризированным сообщением.
     *
     * @param message шаблон сообщения
     * @param args    аргументы для подстановки в шаблон
     * @return поставщик исключения
     */
    public static Supplier<NotFoundException> notFoundException(String message, Object... args) {
        return () -> new NotFoundException(message, args);
    }

    /**
     * Возвращает поставщика исключения с фиксированным сообщением.
     *
     * @param message текстовое сообщение
     * @return поставщик исключения
     */
    public static Supplier<NotFoundException> notFoundException(String message) {
        return () -> new NotFoundException(message);
    }
}