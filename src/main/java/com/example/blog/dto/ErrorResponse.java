package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for error responses
 * Used for consistent error messaging across authentication endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    
    private int status;
    
    private long timestamp;
    
    /**
     * Constructor that automatically sets timestamp to current time
     */
    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
}
