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
 * Unit tests for LoginRequest DTO validation
 * Tests Bean Validation annotations for Requirements 9.2, 9.3
 */
class LoginRequestTest {
    
    private static Validator validator;
    
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidLoginRequestWithUsername_NoViolations() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid login request with username should have no violations");
    }
    
    @Test
    void testValidLoginRequestWithEmail_NoViolations() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid login request with email should have no violations");
    }
    
    @Test
    void testBlankUsernameOrEmail_HasViolation() {
        LoginRequest request = new LoginRequest("", "password123");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank username or email should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username or email is required")));
    }
    
    @Test
    void testNullUsernameOrEmail_HasViolation() {
        LoginRequest request = new LoginRequest(null, "password123");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Null username or email should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username or email is required")));
    }
    
    @Test
    void testBlankPassword_HasViolation() {
        LoginRequest request = new LoginRequest("testuser", "");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Blank password should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }
    
    @Test
    void testNullPassword_HasViolation() {
        LoginRequest request = new LoginRequest("testuser", null);
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Null password should have violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }
    
    @Test
    void testBothFieldsBlank_HasMultipleViolations() {
        LoginRequest request = new LoginRequest("", "");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertEquals(2, violations.size(), "Both blank fields should have 2 violations");
    }
    
    @Test
    void testWhitespaceOnlyUsernameOrEmail_HasViolation() {
        LoginRequest request = new LoginRequest("   ", "password123");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Whitespace-only username or email should have violations");
    }
    
    @Test
    void testWhitespaceOnlyPassword_HasViolation() {
        LoginRequest request = new LoginRequest("testuser", "   ");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty(), "Whitespace-only password should have violations");
    }
}
