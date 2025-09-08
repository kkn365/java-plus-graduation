package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.model.ApiError;
import ru.practicum.ewm.exception.model.StartAfterEndException;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.error("500 {}", e.getMessage(), e);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error ...", e.getMessage(), getStackTrace(e));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleOnMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "MissingServletRequestParameterException Validation Exception.",
                e.getMessage(), getStackTrace(e));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleOnStartAfterEndException(final StartAfterEndException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "StartAfterEndException.",
                e.getMessage(), getStackTrace(e));
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}