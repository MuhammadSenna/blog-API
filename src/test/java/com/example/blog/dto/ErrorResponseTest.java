package com.example.blog.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorResponse DTO
 * Tests error response structure for consistent error messaging
 */
class ErrorResponseTest {
    
    @Test
    void testErrorResponseWithAllFields() {
        String message = "Test error message";
        int status = 400;
        long timestamp = System.currentTimeMillis();
        
        ErrorResponse response = new ErrorResponse(message, status, timestamp);
        
        assertEquals(message, response.getMessage());
        assertEquals(status, response.getStatus());
        assertEquals(timestamp, response.getTimestamp());
    }
    
    @Test
    void testErrorResponseWithTwoArgConstructor_AutoGeneratesTimestamp() {
        String message = "Test error message";
        int status = 401;
        long beforeCreation = System.currentTimeMillis();
        
        ErrorResponse response = new ErrorResponse(message, status);
        
        long afterCreation = System.currentTimeMillis();
        
        assertEquals(message, response.getMessage());
        assertEquals(status, response.getStatus());
        assertTrue(response.getTimestamp() >= beforeCreation, 
                "Timestamp should be at or after creation time");
        assertTrue(response.getTimestamp() <= afterCreation, 
                "Timestamp should be at or before completion time");
    }
    
    @Test
    void testErrorResponseNoArgsConstructor() {
        ErrorResponse response = new ErrorResponse();
        
        assertNotNull(response);
    }
    
    @Test
    void testErrorResponseSetters() {
        ErrorResponse response = new ErrorResponse();
        
        response.setMessage("New error message");
        response.setStatus(500);
        response.setTimestamp(1234567890L);
        
        assertEquals("New error message", response.getMessage());
        assertEquals(500, response.getStatus());
        assertEquals(1234567890L, response.getTimestamp());
    }
    
    @Test
    void testErrorResponseForBadRequest() {
        ErrorResponse response = new ErrorResponse("Username already exists", 400);
        
        assertEquals("Username already exists", response.getMessage());
        assertEquals(400, response.getStatus());
        assertTrue(response.getTimestamp() > 0);
    }
    
    @Test
    void testErrorResponseForUnauthorized() {
        ErrorResponse response = new ErrorResponse("Invalid username or password", 401);
        
        assertEquals("Invalid username or password", response.getMessage());
        assertEquals(401, response.getStatus());
        assertTrue(response.getTimestamp() > 0);
    }
    
    @Test
    void testErrorResponseForForbidden() {
        ErrorResponse response = new ErrorResponse("Access denied", 403);
        
        assertEquals("Access denied", response.getMessage());
        assertEquals(403, response.getStatus());
        assertTrue(response.getTimestamp() > 0);
    }
    
    @Test
    void testErrorResponseEquality() {
        long timestamp = System.currentTimeMillis();
        ErrorResponse response1 = new ErrorResponse("Error", 400, timestamp);
        ErrorResponse response2 = new ErrorResponse("Error", 400, timestamp);
        
        assertEquals(response1, response2, "Responses with same values should be equal");
    }
    
    @Test
    void testErrorResponseToString() {
        ErrorResponse response = new ErrorResponse("Test error", 400);
        
        String toString = response.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Test error"));
        assertTrue(toString.contains("400"));
    }
}
