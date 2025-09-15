package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * Исключение, выбрасываемое при конфликте данных или операций, которые нельзя выполнить из-за текущего состояния системы.
 * <p>
 * Используется для обработки ситуаций, когда действие невозможно из-за несоответствия условий (например, попытка создать объект с уже существующим идентификатором).
 */
public class ConflictException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7092856392954898635L;

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текстовое сообщение
     */
    public ConflictException(String message) {
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
    public ConflictException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    /**
     * Конструктор с указанием причины исключения.
     * <p>
     * Позволяет оборачивать другие исключения.
     *
     * @param cause исходное исключение
     */
    public ConflictException(Throwable cause) {
        super(cause);
    }

    /**
     * Конструктор с сообщением и причиной исключения.
     *
     * @param message текстовое сообщение
     * @param cause   исходное исключение
     */
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Возвращает поставщика исключения с параметризированным сообщением.
     *
     * @param message шаблон сообщения
     * @param args    аргументы для подстановки в шаблон
     * @return поставщик исключения
     */
    public static Supplier<ConflictException> conflictException(String message, Object... args) {
        return () -> new ConflictException(message, args);
    }
}