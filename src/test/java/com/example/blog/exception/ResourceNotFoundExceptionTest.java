package com.example.blog.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceNotFoundException
 */
class ResourceNotFoundExceptionTest {

    @Test
    void testConstructorGeneratesCorrectMessage() {
        // Given
        String resourceType = "Post";
        Long resourceId = 123L;
        
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);
        
        // Then
        assertEquals("Post not found with id: 123", exception.getMessage());
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }
    
    @Test
    void testConstructorWithDifferentResourceType() {
        // Given
        String resourceType = "Comment";
        Long resourceId = 456L;
        
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);
        
        // Then
        assertEquals("Comment not found with id: 456", exception.getMessage());
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }
    
    @Test
    void testExceptionIsRuntimeException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("User", 789L);
        
        // Then
        assertTrue(exception instanceof RuntimeException);
    }
    
    @Test
    void testGettersReturnCorrectValues() {
        // Given
        String resourceType = "Category";
        Long resourceId = 999L;
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);
        
        // When & Then
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }
}
