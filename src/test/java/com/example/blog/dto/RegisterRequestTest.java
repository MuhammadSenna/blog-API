package com.example.blog.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RegisterRequest DTO validation
 * Tests Bean Validation annotations for Requirements 8.2, 8.3, 8.4
 */
class RegisterRequestTest {
    
    private static Validator validator;
    
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidRegisterRequest_NoViolations() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }
    
    @Test
    void testBlankUsername_HasViolation() {
        RegisterRequest request = new RegisterRequest("", "test@example.com", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank username should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username is required")));
    }
    
    @Test
    void testUsernameTooShort_HasViolation() {
        RegisterRequest request = new RegisterRequest("ab", "test@example.com", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Username too short should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username must be between 3 and 50 characters")));
    }
    
    @Test
    void testUsernameTooLong_HasViolation() {
        String longUsername = "a".repeat(51);
        RegisterRequest request = new RegisterRequest(longUsername, "test@example.com", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Username too long should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username must be between 3 and 50 characters")));
    }
    
    @Test
    void testBlankEmail_HasViolation() {
        RegisterRequest request = new RegisterRequest("testuser", "", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank email should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }
    
    @Test
    void testInvalidEmailFormat_HasViolation() {
        RegisterRequest request = new RegisterRequest("testuser", "invalid-email", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Invalid email format should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }
    
    @Test
    void testEmailTooLong_HasViolation() {
        String longEmail = "a".repeat(90) + "@example.com"; // Over 100 characters
        RegisterRequest request = new RegisterRequest("testuser", longEmail, "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Email too long should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must not exceed 100 characters")));
    }
    
    @Test
    void testBlankPassword_HasViolation() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank password should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }
    
    @Test
    void testPasswordTooShort_HasViolation() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "12345");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Password too short should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password must be at least 6 characters")));
    }
    
    @Test
    void testMinimumValidUsername_NoViolations() {
        RegisterRequest request = new RegisterRequest("abc", "test@example.com", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Username with 3 characters should be valid");
    }
    
    @Test
    void testMaximumValidUsername_NoViolations() {
        String maxUsername = "a".repeat(50);
        RegisterRequest request = new RegisterRequest(maxUsername, "test@example.com", "password123");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Username with 50 characters should be valid");
    }
    
    @Test
    void testMinimumValidPassword_NoViolations() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "123456");
        
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Password with 6 characters should be valid");
    }
}
