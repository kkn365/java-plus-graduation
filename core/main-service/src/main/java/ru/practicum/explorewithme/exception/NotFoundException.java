package ru.practicum.explorewithme.exception;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public static Supplier<NotFoundException> notFoundException(String message, Object... args) {
        return () -> new NotFoundException(message, args);
    }

    public static Supplier<NotFoundException> notFoundException(String message) {
        return () -> new NotFoundException(message);
    }
}
