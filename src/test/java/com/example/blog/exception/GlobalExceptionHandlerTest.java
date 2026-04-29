package com.example.blog.exception;

import com.example.blog.dto.ErrorResponse;
import com.example.blog.dto.ValidationErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404WithErrorMessage() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Post", 123L);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Post not found with id: 123", response.getBody().getMessage());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleValidationException_ShouldReturn400WithValidationErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("registerRequest", "username", "Username is required");
        FieldError fieldError2 = new FieldError("registerRequest", "email", "Email must be valid");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ValidationErrorResponse> response = exceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertTrue(response.getBody().getTimestamp() > 0);
        assertNotNull(response.getBody().getErrors());
        assertEquals(2, response.getBody().getErrors().size());
        assertEquals("Username is required", response.getBody().getErrors().get("username"));
        assertEquals("Email must be valid", response.getBody().getErrors().get("email"));
    }

    @Test
    void handleAccessDeniedException_ShouldReturn403WithErrorMessage() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Access denied", response.getBody().getMessage());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleAuthenticationException_ShouldReturn401WithErrorMessage() {
        // Arrange
        AuthenticationException ex = new BadCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleGenericException_ShouldReturn500WithErrorMessage() {
        // Arrange
        Exception ex = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An internal error occurred", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("Something went wrong")); // Should not expose internal details
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleValidationException_ShouldIncludeAllFieldErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("loginRequest", "password", "Password cannot be blank");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ValidationErrorResponse> response = exceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("Password cannot be blank", response.getBody().getErrors().get("password"));
    }

    @Test
    void handleAuthenticationException_ShouldHandleDifferentAuthExceptions() {
        // Arrange
        AuthenticationException ex = new BadCredentialsException("User not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }
}
