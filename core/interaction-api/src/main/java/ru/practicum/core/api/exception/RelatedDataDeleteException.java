package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Исключение, выбрасываемое при попытке удалить данные, на которые есть внешние ссылки.
 * <p>
 * Используется для обработки ситуаций, когда удаление объекта невозможно из-за зависимых данных.
 */
public class RelatedDataDeleteException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -892134765987654321L;

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текстовое сообщение об ошибке
     */
    public RelatedDataDeleteException(String message) {
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
    public RelatedDataDeleteException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    /**
     * Конструктор с указанием причины исключения.
     * <p>
     * Позволяет оборачивать другие исключения.
     *
     * @param cause исходное исключение
     */
    public RelatedDataDeleteException(Throwable cause) {
        super(cause);
    }

    /**
     * Конструктор с сообщением и причиной исключения.
     *
     * @param message текстовое сообщение
     * @param cause   исходное исключение
     */
    public RelatedDataDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}