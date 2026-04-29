package com.example.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Error response for validation failures
 * Extends ErrorResponse with additional field-level validation errors
 * 
 * Validates Requirements: 7.5, 7.6
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {
    
    /**
     * Map of field names to validation error messages
     * Example: {"title": "must not be blank", "content": "size must be between 1 and 200"}
     */
    private Map<String, String> errors;
    
    /**
     * Constructor with all fields
     */
    public ValidationErrorResponse(String message, int status, long timestamp, Map<String, String> errors) {
        super(message, status, timestamp);
        this.errors = errors;
    }
    
    /**
     * Constructor that automatically sets timestamp to current time
     */
    public ValidationErrorResponse(String message, int status, Map<String, String> errors) {
        super(message, status);
        this.errors = errors;
    }
}
