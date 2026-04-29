package com.example.blog.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateCommentRequest validation
 * Tests: Requirements 1.6, 2.6
 */
class CreateCommentRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCreateCommentRequest() {
        CreateCommentRequest request = new CreateCommentRequest("This is a valid comment");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testBlankContent() {
        CreateCommentRequest request = new CreateCommentRequest("");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testNullContent() {
        CreateCommentRequest request = new CreateCommentRequest(null);

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testWhitespaceOnlyContent() {
        CreateCommentRequest request = new CreateCommentRequest("   ");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Whitespace-only content should be invalid");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testLongContent() {
        String longContent = "a".repeat(10000);
        CreateCommentRequest request = new CreateCommentRequest(longContent);

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Long content should be valid (no max length constraint)");
    }
}
