package com.example.blog.exception;

import com.example.blog.dto.ErrorResponse;
import com.example.blog.dto.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 * Provides consistent error responses across all endpoints
 * 
 * Validates Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException
     * Returns 404 Not Found with error details
     * 
     * Validates Requirement: 7.2
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles validation errors from @Valid annotations
     * Returns 400 Bad Request with field-specific validation errors
     * 
     * Validates Requirements: 7.3, 7.6
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed: {}", errors);
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            "Validation failed",
            HttpStatus.BAD_REQUEST.value(),
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles access denied exceptions
     * Returns 403 Forbidden with error message
     * 
     * Validates Requirement: 7.4
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Access denied",
            HttpStatus.FORBIDDEN.value()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles authentication failures
     * Returns 401 Unauthorized with error message
     * 
     * Validates Requirement: 7.5
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles all other uncaught exceptions
     * Returns 500 Internal Server Error
     * 
     * Validates Requirements: 7.7, 7.8
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Internal server error: {}", ex.getMessage(), ex);
        
        // Don't expose sensitive information or stack traces in response
        ErrorResponse errorResponse = new ErrorResponse(
            "An internal error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
