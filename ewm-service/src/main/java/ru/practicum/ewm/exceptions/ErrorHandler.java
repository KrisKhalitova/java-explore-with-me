package ru.practicum.ewm.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(final RuntimeException e) {
        log.debug("Получен статус 409 Conflict {}", e.getMessage(), e);
        return new ErrorResponse("Получен статус 409 Conflict", e.getMessage());
    }


    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleValidationException(ValidationException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Ошибка в запросе", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ErrorResponse handleNotFoundException(NotFoundException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Ошибка в запросе", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    protected ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Получен статус 409 Conflict", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    protected ErrorResponse handleInternalServerError(HttpServerErrorException.InternalServerError e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Получен статус 409 Conflict", e.getMessage());
    }
}
