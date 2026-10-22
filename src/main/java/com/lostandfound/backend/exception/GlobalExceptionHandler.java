package com.lostandfound.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException e) {
        return error(e.getStatus(), e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "fields", fields));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return error(HttpStatus.CONFLICT, "A record with the same unique value already exists");
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing, malformed, or has an invalid value");
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + e.getName() + "'");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethod(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed for this URL");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json");
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Not found");
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, "You do not have permission to do this");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException e) {
        return error(HttpStatus.UNAUTHORIZED, "Not logged in, or your session has expired");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException e) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "File is too large (max 5 MB)");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPart(MissingServletRequestPartException e) {
        return error(HttpStatus.BAD_REQUEST,
                "Missing file: send it in a form field named '" + e.getRequestPartName() + "'");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException e) {
        return error(HttpStatus.BAD_REQUEST, "Missing parameter '" + e.getParameterName() + "'");
    }
}