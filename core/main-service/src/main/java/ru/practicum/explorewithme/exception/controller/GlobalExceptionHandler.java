package ru.practicum.explorewithme.exception.controller;

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
import ru.practicum.explorewithme.exception.*;
import ru.practicum.explorewithme.exception.model.ApiError;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.error("{} {}", HttpStatus.NOT_FOUND, e.getMessage());
        return new ApiError(HttpStatus.NOT_FOUND, "The requested object was not found.", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final MethodArgumentNotValidException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Error with the input parameter.", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Validation exception: ", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final HandlerMethodValidationException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Validation exception: ", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataAlreadyExistException(final DataAlreadyExistException e) {
        log.error("{} {}", HttpStatus.CONFLICT, e.getMessage());
        return new ApiError(HttpStatus.CONFLICT, "The data must be unique.", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataAlreadyExistException(final ConflictException e) {
        log.error("{} {}", HttpStatus.CONFLICT, e.getMessage());
        return new ApiError(HttpStatus.CONFLICT, "Runtime conflict", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRelatedDataDeleteException(final RelatedDataDeleteException e) {
        log.error("{} {}", HttpStatus.CONFLICT, e.getMessage());
        return new ApiError(HttpStatus.CONFLICT, "Deleting related data is not allowed.", e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Throwable e) {
        String getStackTrace = getStackTrace(e);
        log.error("{} {} {}", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), getStackTrace);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error ...", e.getMessage(), getStackTrace);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleOnConstraintValidationException(final ConstraintViolationException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Constraint Validation Exception.", e.getMessage(), getStackTrace(e));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleOnMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error("{} {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "MissingServletRequestParameterException Validation Exception.",
                e.getMessage(), getStackTrace(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(final ForbiddenException e) {
        log.error("{} {}", HttpStatus.FORBIDDEN, e.getMessage());
        return new ApiError(HttpStatus.FORBIDDEN, "Access denied...",
                e.getMessage(), getStackTrace(e));
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}