package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response containing JWT token and user information
 * Validates: Requirement 9.3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    
    private String tokenType = "Bearer";
    
    private String username;
    
    private String email;
    
    /**
     * Constructor for creating response with token and user details
     * TokenType defaults to "Bearer"
     */
    public AuthResponse(String token, String username, String email) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.email = email;
    }
}
