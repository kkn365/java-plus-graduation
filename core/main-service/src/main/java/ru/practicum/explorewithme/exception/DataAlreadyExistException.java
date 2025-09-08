package ru.practicum.explorewithme.exception;

import java.text.MessageFormat;

public class DataAlreadyExistException extends RuntimeException {

    public DataAlreadyExistException(String message) {
        super(message);
    }

    public DataAlreadyExistException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }
}