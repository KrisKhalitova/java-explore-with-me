package ru.practicum.ewm.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class ResponseException extends ResponseStatusException {

    private final String message;

    public ResponseException(HttpStatus status, String message) {
        super(status, message);
        this.message = message;
    }
}