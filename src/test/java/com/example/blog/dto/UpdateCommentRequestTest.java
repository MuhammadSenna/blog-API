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
 * Unit tests for UpdateCommentRequest validation
 * Tests: Requirements 1.7, 2.7
 */
class UpdateCommentRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUpdateCommentRequest() {
        UpdateCommentRequest request = new UpdateCommentRequest("This is an updated comment");

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testBlankContent() {
        UpdateCommentRequest request = new UpdateCommentRequest("");

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testNullContent() {
        UpdateCommentRequest request = new UpdateCommentRequest(null);

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testWhitespaceOnlyContent() {
        UpdateCommentRequest request = new UpdateCommentRequest("   ");

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Whitespace-only content should be invalid");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testLongContent() {
        String longContent = "a".repeat(10000);
        UpdateCommentRequest request = new UpdateCommentRequest(longContent);

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Long content should be valid (no max length constraint)");
    }
}
