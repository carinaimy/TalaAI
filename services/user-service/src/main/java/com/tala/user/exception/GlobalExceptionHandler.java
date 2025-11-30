package com.tala.user.exception;

import com.tala.core.exception.TalaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for user-service
 * Ensures all exceptions return proper JSON error responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TalaException.class)
    public ResponseEntity<ErrorResponse> handleTalaException(TalaException ex) {
        log.error("TalaException: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode().getCode());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(ex.getMessage())
            .code(ex.getErrorCode().getCode())
            .build();
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Validation failed")
            .validationErrors(errors)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("An unexpected error occurred")
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Map error code to HTTP status
     */
    private HttpStatus mapErrorCodeToHttpStatus(int errorCode) {
        // General errors (1xxx)
        if (errorCode >= 1000 && errorCode < 2000) {
            return switch (errorCode) {
                case 1001 -> HttpStatus.BAD_REQUEST; // VALIDATION_ERROR
                case 1002 -> HttpStatus.NOT_FOUND; // NOT_FOUND
                case 1003 -> HttpStatus.CONFLICT; // ALREADY_EXISTS
                case 1004 -> HttpStatus.UNAUTHORIZED; // UNAUTHORIZED
                case 1005 -> HttpStatus.FORBIDDEN; // FORBIDDEN
                case 1006 -> HttpStatus.BAD_REQUEST; // BAD_REQUEST
                case 1007 -> HttpStatus.CONFLICT; // CONFLICT
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        }
        
        // User errors (3xxx)
        if (errorCode >= 3000 && errorCode < 4000) {
            return switch (errorCode) {
                case 3000 -> HttpStatus.NOT_FOUND; // USER_NOT_FOUND
                case 3001 -> HttpStatus.NOT_FOUND; // PROFILE_NOT_FOUND
                case 3002 -> HttpStatus.CONFLICT; // USER_ALREADY_EXISTS
                case 3003 -> HttpStatus.UNAUTHORIZED; // INVALID_CREDENTIALS
                case 3004 -> HttpStatus.FORBIDDEN; // EMAIL_NOT_VERIFIED
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        }
        
        // Default to 500 for unknown error codes
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
