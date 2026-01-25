package com.hrms.api.exception;

import com.hrms.service.exception.BusinessException;
import com.hrms.service.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import one.util.streamex.StreamEx;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<Map<String, String>> handleLockException(CannotAcquireLockException ex) {
        var msg = ex.getMessage();
        if (msg != null && msg.contains("не найден")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Конфликт с другой операцией, попробуйте снова"));
    }

    @ExceptionHandler(org.hibernate.exception.LockAcquisitionException.class)
    public ResponseEntity<Map<String, String>> handleHibernateLockException(org.hibernate.exception.LockAcquisitionException ex) {
        var msg = ex.getMessage();
        if (msg != null && msg.contains("не найден")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Конфликт с другой операцией, попробуйте снова"));
    }

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
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return handleConstraintViolation(cve);
            }
            if (cause instanceof BusinessException be) {
                return handleBusinessException(be);
            }
            if (cause instanceof EntityNotFoundException enfe) {
                return handleEntityNotFound(enfe);
            }
            cause = cause.getCause();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Конфликт транзакции, попробуйте снова"));
    }

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(org.springframework.transaction.TransactionSystemException ex) {
        var root = ex.getRootCause();
        if (root instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(cve);
        }
        var cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof BusinessException be) {
                return handleBusinessException(be);
            }
            if (cause instanceof EntityNotFoundException enfe) {
                return handleEntityNotFound(enfe);
            }
            cause = cause.getCause();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Конфликт транзакции, попробуйте снова"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccessException(org.springframework.dao.DataAccessException ex) {
        var cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof BusinessException be) {
                return handleBusinessException(be);
            }
            if (cause instanceof EntityNotFoundException enfe) {
                return handleEntityNotFound(enfe);
            }
            cause = cause.getCause();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Конфликт данных, попробуйте снова"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        var cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof BusinessException be) {
                return handleBusinessException(be);
            }
            if (cause instanceof EntityNotFoundException enfe) {
                return handleEntityNotFound(enfe);
            }
            cause = cause.getCause();
        }
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
