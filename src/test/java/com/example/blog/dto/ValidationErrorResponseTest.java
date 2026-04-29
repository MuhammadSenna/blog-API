package com.example.blog.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationErrorResponse
 * Tests validation error response structure for Requirements 7.5, 7.6
 */
class ValidationErrorResponseTest {
    
    @Test
    void testValidationErrorResponseWithAllFields() {
        String message = "Validation failed";
        int status = 400;
        long timestamp = System.currentTimeMillis();
        Map<String, String> errors = new HashMap<>();
        errors.put("title", "must not be blank");
        errors.put("content", "size must be between 1 and 200");
        
        ValidationErrorResponse response = new ValidationErrorResponse(message, status, timestamp, errors);
        
        assertEquals(message, response.getMessage());
        assertEquals(status, response.getStatus());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals(errors, response.getErrors());
        assertEquals(2, response.getErrors().size());
    }
    
    @Test
    void testValidationErrorResponseWithAutoTimestamp() {
        String message = "Validation failed";
        int status = 400;
        Map<String, String> errors = new HashMap<>();
        errors.put("email", "must be a valid email");
        
        long beforeCreation = System.currentTimeMillis();
        ValidationErrorResponse response = new ValidationErrorResponse(message, status, errors);
        long afterCreation = System.currentTimeMillis();
        
        assertEquals(message, response.getMessage());
        assertEquals(status, response.getStatus());
        assertEquals(errors, response.getErrors());
        assertTrue(response.getTimestamp() >= beforeCreation);
        assertTrue(response.getTimestamp() <= afterCreation);
    }
    
    @Test
    void testValidationErrorResponseNoArgsConstructor() {
        ValidationErrorResponse response = new ValidationErrorResponse();
        
        assertNotNull(response);
        assertNull(response.getMessage());
        assertEquals(0, response.getStatus());
        assertNull(response.getErrors());
    }
    
    @Test
    void testValidationErrorResponseSetters() {
        ValidationErrorResponse response = new ValidationErrorResponse();
        
        response.setMessage("New validation error");
        response.setStatus(400);
        response.setTimestamp(123456789L);
        Map<String, String> errors = new HashMap<>();
        errors.put("field1", "error1");
        response.setErrors(errors);
        
        assertEquals("New validation error", response.getMessage());
        assertEquals(400, response.getStatus());
        assertEquals(123456789L, response.getTimestamp());
        assertEquals(errors, response.getErrors());
    }
    
    @Test
    void testValidationErrorResponseWithEmptyErrors() {
        Map<String, String> errors = new HashMap<>();
        ValidationErrorResponse response = new ValidationErrorResponse("Validation failed", 400, errors);
        
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }
    
    @Test
    void testValidationErrorResponseWithMultipleErrors() {
        Map<String, String> errors = new HashMap<>();
        errors.put("title", "must not be blank");
        errors.put("content", "must not be blank");
        errors.put("categoryId", "must not be null");
        
        ValidationErrorResponse response = new ValidationErrorResponse("Validation failed", 400, errors);
        
        assertEquals(3, response.getErrors().size());
        assertTrue(response.getErrors().containsKey("title"));
        assertTrue(response.getErrors().containsKey("content"));
        assertTrue(response.getErrors().containsKey("categoryId"));
    }
    
    @Test
    void testValidationErrorResponseInheritsFromErrorResponse() {
        ValidationErrorResponse response = new ValidationErrorResponse("Error", 400, new HashMap<>());
        
        assertTrue(response instanceof ErrorResponse, "ValidationErrorResponse should extend ErrorResponse");
    }
    
    @Test
    void testValidationErrorResponseToString() {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", "error message");
        ValidationErrorResponse response = new ValidationErrorResponse("Validation failed", 400, errors);
        
        String toString = response.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Validation failed") || toString.contains("field"));
    }
    
    @Test
    void testValidationErrorResponseEquality() {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", "error");
        
        ValidationErrorResponse response1 = new ValidationErrorResponse("Error", 400, 123456L, errors);
        ValidationErrorResponse response2 = new ValidationErrorResponse("Error", 400, 123456L, errors);
        
        assertEquals(response1, response2, "ValidationErrorResponses with same values should be equal");
    }
}
