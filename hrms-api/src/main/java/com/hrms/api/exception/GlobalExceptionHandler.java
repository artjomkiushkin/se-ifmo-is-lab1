package com.hrms.api.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import one.util.streamex.StreamEx;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        var message = StreamEx.of(ex.getConstraintViolations())
            .map(ConstraintViolation::getMessage)
            .joining("; ");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var message = StreamEx.of(ex.getBindingResult().getFieldErrors())
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .joining("; ");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
    }

    @ExceptionHandler(jakarta.persistence.RollbackException.class)
    public ResponseEntity<Map<String, String>> handleRollbackException(jakarta.persistence.RollbackException ex) {
        var cause = ex.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(cve);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", extractMessage(ex)));
    }

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(org.springframework.transaction.TransactionSystemException ex) {
        var root = ex.getRootCause();
        if (root instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(cve);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", extractMessage(ex)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", extractMessage(ex)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Внутренняя ошибка сервера"));
    }

    private String extractMessage(Throwable ex) {
        var cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            if (cause.getCause() instanceof ConstraintViolationException cve) {
                return StreamEx.of(cve.getConstraintViolations())
                    .map(ConstraintViolation::getMessage)
                    .joining("; ");
            }
            cause = cause.getCause();
        }
        return ex.getMessage() != null ? ex.getMessage() : "Ошибка обработки запроса";
    }
}
