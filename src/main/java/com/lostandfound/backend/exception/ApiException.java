package com.lostandfound.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * An error we raise on purpose, with the HTTP status to answer and a message the client can show.
 * GlobalExceptionHandler turns it into {"error": message}.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}