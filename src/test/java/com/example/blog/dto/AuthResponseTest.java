package com.example.blog.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthResponse DTO
 * Tests response structure for Requirement 9.3
 */
class AuthResponseTest {
    
    @Test
    void testAuthResponseWithAllFields() {
        String token = "eyJhbGciOiJIUzUxMiJ9.test.token";
        String username = "testuser";
        String email = "test@example.com";
        
        AuthResponse response = new AuthResponse(token, "Bearer", username, email);
        
        assertEquals(token, response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());
    }
    
    @Test
    void testAuthResponseWithThreeArgConstructor_DefaultsTokenTypeToBearer() {
        String token = "eyJhbGciOiJIUzUxMiJ9.test.token";
        String username = "testuser";
        String email = "test@example.com";
        
        AuthResponse response = new AuthResponse(token, username, email);
        
        assertEquals(token, response.getToken());
        assertEquals("Bearer", response.getTokenType(), "TokenType should default to Bearer");
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());
    }
    
    @Test
    void testAuthResponseNoArgsConstructor() {
        AuthResponse response = new AuthResponse();
        
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType(), "TokenType should default to Bearer");
    }
    
    @Test
    void testAuthResponseSetters() {
        AuthResponse response = new AuthResponse();
        
        response.setToken("test.jwt.token");
        response.setTokenType("Bearer");
        response.setUsername("newuser");
        response.setEmail("new@example.com");
        
        assertEquals("test.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("newuser", response.getUsername());
        assertEquals("new@example.com", response.getEmail());
    }
    
    @Test
    void testAuthResponseEquality() {
        AuthResponse response1 = new AuthResponse("token", "testuser", "test@example.com");
        AuthResponse response2 = new AuthResponse("token", "testuser", "test@example.com");
        
        assertEquals(response1, response2, "Responses with same values should be equal");
    }
    
    @Test
    void testAuthResponseToString() {
        AuthResponse response = new AuthResponse("token", "testuser", "test@example.com");
        
        String toString = response.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("token"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
    }
}
