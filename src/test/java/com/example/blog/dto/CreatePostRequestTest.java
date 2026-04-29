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
 * Unit tests for CreatePostRequest validation
 * Tests: Requirements 1.4, 2.2, 2.3
 */
class CreatePostRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCreatePostRequest() {
        CreatePostRequest request = new CreatePostRequest(
            "Valid Title",
            "Valid content for the blog post",
            1L,
            Set.of(1L, 2L)
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testBlankTitle() {
        CreatePostRequest request = new CreatePostRequest(
            "",
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testNullTitle() {
        CreatePostRequest request = new CreatePostRequest(
            null,
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTitleExceedsMaxLength() {
        String longTitle = "a".repeat(201);
        CreatePostRequest request = new CreatePostRequest(
            longTitle,
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTitleAtMaxLength() {
        String maxTitle = "a".repeat(200);
        CreatePostRequest request = new CreatePostRequest(
            maxTitle,
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Title at max length should be valid");
    }

    @Test
    void testBlankContent() {
        CreatePostRequest request = new CreatePostRequest(
            "Valid Title",
            "",
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testNullContent() {
        CreatePostRequest request = new CreatePostRequest(
            "Valid Title",
            null,
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testNullCategoryId() {
        CreatePostRequest request = new CreatePostRequest(
            "Valid Title",
            "Valid content",
            null,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("categoryId")));
    }

    @Test
    void testNullTagIds() {
        CreatePostRequest request = new CreatePostRequest(
            "Valid Title",
            "Valid content",
            1L,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Null tagIds should be valid (optional field)");
    }

    @Test
    void testEmptyTagIds() {
        CreatePostRequest request = new CreatePostRequest(
            "Valid Title",
            "Valid content",
            1L,
            Set.of()
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Empty tagIds should be valid");
    }

    @Test
    void testMultipleViolations() {
        CreatePostRequest request = new CreatePostRequest(
            "",
            "",
            null,
            null
        );

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size(), "Should have violations for title, content, and categoryId");
    }
}
