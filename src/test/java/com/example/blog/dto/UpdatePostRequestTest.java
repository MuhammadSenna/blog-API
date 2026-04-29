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
 * Unit tests for UpdatePostRequest validation
 * Tests: Requirements 1.5, 2.4, 2.5
 */
class UpdatePostRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUpdatePostRequest() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated Title",
            "Updated content for the blog post",
            1L,
            Set.of(1L, 2L)
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testBlankTitle() {
        UpdatePostRequest request = new UpdatePostRequest(
            "",
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testNullTitle() {
        UpdatePostRequest request = new UpdatePostRequest(
            null,
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTitleExceedsMaxLength() {
        String longTitle = "a".repeat(201);
        UpdatePostRequest request = new UpdatePostRequest(
            longTitle,
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTitleAtMaxLength() {
        String maxTitle = "a".repeat(200);
        UpdatePostRequest request = new UpdatePostRequest(
            maxTitle,
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Title at max length should be valid");
    }

    @Test
    void testBlankContent() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Valid Title",
            "",
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testNullContent() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Valid Title",
            null,
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testNullCategoryId() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Valid Title",
            "Valid content",
            null,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("categoryId")));
    }

    @Test
    void testNullTagIds() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Valid Title",
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Null tagIds should be valid (optional field)");
    }

    @Test
    void testEmptyTagIds() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Valid Title",
            "Valid content",
            1L,
            Set.of()
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Empty tagIds should be valid");
    }

    @Test
    void testMultipleViolations() {
        UpdatePostRequest request = new UpdatePostRequest(
            "",
            "",
            null,
            null
        );

        Set<ConstraintViolation<UpdatePostRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size(), "Should have violations for title, content, and categoryId");
    }
}
