package com.example.blog.controller;

import com.example.blog.dto.CommentDTO;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentController
 * Tests GET endpoints for retrieving comments
 * 
 * Validates Requirements: 4.1, 4.2, 5.8
 */
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    private CommentController commentController;

    @BeforeEach
    void setUp() {
        commentController = new CommentController(commentService);
    }

    @Test
    void testControllerCreation_WithCommentService_Success() {
        // Verify that the controller can be instantiated with CommentService dependency
        assertNotNull(commentController);
    }

    // ========== Tests for GET /api/posts/{postId}/comments ==========

    @Test
    void testGetCommentsByPostId_WithExistingPost_ReturnsCommentList() {
        // Arrange
        Long postId = 1L;
        CommentDTO comment1 = createTestCommentDTO(1L, "First comment", "user1");
        CommentDTO comment2 = createTestCommentDTO(2L, "Second comment", "user2");
        List<CommentDTO> expectedComments = Arrays.asList(comment1, comment2);
        
        when(commentService.getCommentsByPostId(postId)).thenReturn(expectedComments);
        
        // Act
        ResponseEntity<List<CommentDTO>> response = commentController.getCommentsByPostId(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("First comment", response.getBody().get(0).getContent());
        assertEquals("user1", response.getBody().get(0).getAuthorUsername());
        assertEquals("Second comment", response.getBody().get(1).getContent());
        assertEquals("user2", response.getBody().get(1).getAuthorUsername());
        
        verify(commentService, times(1)).getCommentsByPostId(postId);
    }

    @Test
    void testGetCommentsByPostId_WithEmptyComments_ReturnsEmptyList() {
        // Arrange
        Long postId = 1L;
        List<CommentDTO> emptyComments = Arrays.asList();
        
        when(commentService.getCommentsByPostId(postId)).thenReturn(emptyComments);
        
        // Act
        ResponseEntity<List<CommentDTO>> response = commentController.getCommentsByPostId(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        
        verify(commentService, times(1)).getCommentsByPostId(postId);
    }

    @Test
    void testGetCommentsByPostId_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentPostId = 999L;
        
        when(commentService.getCommentsByPostId(nonExistentPostId))
                .thenThrow(new ResourceNotFoundException("Post", nonExistentPostId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.getCommentsByPostId(nonExistentPostId)
        );
        
        assertEquals("Post not found with id: 999", exception.getMessage());
        assertEquals("Post", exception.getResourceType());
        assertEquals(nonExistentPostId, exception.getResourceId());
        
        verify(commentService, times(1)).getCommentsByPostId(nonExistentPostId);
    }

    @Test
    void testGetCommentsByPostId_WithDifferentPost_ReturnsCorrectComments() {
        // Arrange
        Long postId = 42L;
        CommentDTO comment = createTestCommentDTO(1L, "Comment on post 42", "user1");
        List<CommentDTO> expectedComments = Arrays.asList(comment);
        
        when(commentService.getCommentsByPostId(postId)).thenReturn(expectedComments);
        
        // Act
        ResponseEntity<List<CommentDTO>> response = commentController.getCommentsByPostId(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Comment on post 42", response.getBody().get(0).getContent());
        
        verify(commentService, times(1)).getCommentsByPostId(postId);
    }

    // ========== Tests for GET /api/posts/{postId}/comments/{commentId} ==========

    @Test
    void testGetCommentById_WithExistingComment_ReturnsComment() {
        // Arrange
        Long postId = 1L;
        Long commentId = 1L;
        CommentDTO expectedComment = createTestCommentDTO(commentId, "Test comment", "testuser");
        
        when(commentService.getCommentById(postId, commentId)).thenReturn(expectedComment);
        
        // Act
        ResponseEntity<CommentDTO> response = commentController.getCommentById(postId, commentId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(commentId, response.getBody().getId());
        assertEquals("Test comment", response.getBody().getContent());
        assertEquals("testuser", response.getBody().getAuthorUsername());
        
        verify(commentService, times(1)).getCommentById(postId, commentId);
    }

    @Test
    void testGetCommentById_WithNonExistentComment_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Long nonExistentCommentId = 999L;
        
        when(commentService.getCommentById(postId, nonExistentCommentId))
                .thenThrow(new ResourceNotFoundException("Comment", nonExistentCommentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.getCommentById(postId, nonExistentCommentId)
        );
        
        assertEquals("Comment not found with id: 999", exception.getMessage());
        assertEquals("Comment", exception.getResourceType());
        assertEquals(nonExistentCommentId, exception.getResourceId());
        
        verify(commentService, times(1)).getCommentById(postId, nonExistentCommentId);
    }

    @Test
    void testGetCommentById_WithCommentNotBelongingToPost_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Long commentId = 1L;
        
        // Comment exists but belongs to a different post
        when(commentService.getCommentById(postId, commentId))
                .thenThrow(new ResourceNotFoundException("Comment", commentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.getCommentById(postId, commentId)
        );
        
        assertEquals("Comment not found with id: 1", exception.getMessage());
        
        verify(commentService, times(1)).getCommentById(postId, commentId);
    }

    @Test
    void testGetCommentById_WithDifferentComment_ReturnsCorrectComment() {
        // Arrange
        Long postId = 1L;
        Long commentId = 42L;
        CommentDTO expectedComment = createTestCommentDTO(commentId, "Another comment", "anotheruser");
        
        when(commentService.getCommentById(postId, commentId)).thenReturn(expectedComment);
        
        // Act
        ResponseEntity<CommentDTO> response = commentController.getCommentById(postId, commentId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(commentId, response.getBody().getId());
        assertEquals("Another comment", response.getBody().getContent());
        assertEquals("anotheruser", response.getBody().getAuthorUsername());
        
        verify(commentService, times(1)).getCommentById(postId, commentId);
    }

    @Test
    void testGetCommentById_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentPostId = 999L;
        Long commentId = 1L;
        
        when(commentService.getCommentById(nonExistentPostId, commentId))
                .thenThrow(new ResourceNotFoundException("Comment", commentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.getCommentById(nonExistentPostId, commentId)
        );
        
        assertEquals("Comment not found with id: 1", exception.getMessage());
        
        verify(commentService, times(1)).getCommentById(nonExistentPostId, commentId);
    }

    /**
     * Helper method to create a test CommentDTO
     */
    private CommentDTO createTestCommentDTO(Long id, String content, String authorUsername) {
        return new CommentDTO(
                id,
                content,
                authorUsername,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
    }

    // ========== Tests for POST /api/posts/{postId}/comments ==========

    @Test
    void testCreateComment_WithValidRequest_ReturnsCreatedComment() {
        // Arrange
        Long postId = 1L;
        String username = "testuser";
        CreateCommentRequest request = new CreateCommentRequest("This is a new comment");
        CommentDTO expectedComment = createTestCommentDTO(1L, "This is a new comment", username);
        
        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            
            when(commentService.createComment(postId, request, username)).thenReturn(expectedComment);
            
            // Act
            ResponseEntity<CommentDTO> response = commentController.createComment(postId, request);
            
            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getId());
            assertEquals("This is a new comment", response.getBody().getContent());
            assertEquals(username, response.getBody().getAuthorUsername());
            
            verify(commentService, times(1)).createComment(postId, request, username);
        }
    }

    @Test
    void testCreateComment_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentPostId = 999L;
        String username = "testuser";
        CreateCommentRequest request = new CreateCommentRequest("Comment on non-existent post");
        
        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            
            when(commentService.createComment(nonExistentPostId, request, username))
                    .thenThrow(new ResourceNotFoundException("Post", nonExistentPostId));
            
            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> commentController.createComment(nonExistentPostId, request)
            );
            
            assertEquals("Post not found with id: 999", exception.getMessage());
            verify(commentService, times(1)).createComment(nonExistentPostId, request, username);
        }
    }

    @Test
    void testCreateComment_WithDifferentUser_UsesAuthenticatedUsername() {
        // Arrange
        Long postId = 1L;
        String username = "anotheruser";
        CreateCommentRequest request = new CreateCommentRequest("Comment from another user");
        CommentDTO expectedComment = createTestCommentDTO(2L, "Comment from another user", username);
        
        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            
            when(commentService.createComment(postId, request, username)).thenReturn(expectedComment);
            
            // Act
            ResponseEntity<CommentDTO> response = commentController.createComment(postId, request);
            
            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(username, response.getBody().getAuthorUsername());
            
            verify(commentService, times(1)).createComment(postId, request, username);
        }
    }

    // ========== Tests for PUT /api/posts/{postId}/comments/{commentId} ==========

    @Test
    void testUpdateComment_WithValidRequest_ReturnsUpdatedComment() {
        // Arrange
        Long postId = 1L;
        Long commentId = 1L;
        UpdateCommentRequest request = new UpdateCommentRequest("Updated comment content");
        CommentDTO expectedComment = createTestCommentDTO(commentId, "Updated comment content", "testuser");
        
        when(commentService.updateComment(postId, commentId, request)).thenReturn(expectedComment);
        
        // Act
        ResponseEntity<CommentDTO> response = commentController.updateComment(postId, commentId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(commentId, response.getBody().getId());
        assertEquals("Updated comment content", response.getBody().getContent());
        
        verify(commentService, times(1)).updateComment(postId, commentId, request);
    }

    @Test
    void testUpdateComment_WithNonExistentComment_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Long nonExistentCommentId = 999L;
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
        
        when(commentService.updateComment(postId, nonExistentCommentId, request))
                .thenThrow(new ResourceNotFoundException("Comment", nonExistentCommentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.updateComment(postId, nonExistentCommentId, request)
        );
        
        assertEquals("Comment not found with id: 999", exception.getMessage());
        verify(commentService, times(1)).updateComment(postId, nonExistentCommentId, request);
    }

    @Test
    void testUpdateComment_WithCommentNotBelongingToPost_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Long commentId = 1L;
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
        
        // Comment exists but belongs to a different post
        when(commentService.updateComment(postId, commentId, request))
                .thenThrow(new ResourceNotFoundException("Comment", commentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.updateComment(postId, commentId, request)
        );
        
        assertEquals("Comment not found with id: 1", exception.getMessage());
        verify(commentService, times(1)).updateComment(postId, commentId, request);
    }

    // ========== Tests for DELETE /api/posts/{postId}/comments/{commentId} ==========

    @Test
    void testDeleteComment_WithExistingComment_ReturnsNoContent() {
        // Arrange
        Long postId = 1L;
        Long commentId = 1L;
        
        doNothing().when(commentService).deleteComment(postId, commentId);
        
        // Act
        ResponseEntity<Void> response = commentController.deleteComment(postId, commentId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(commentService, times(1)).deleteComment(postId, commentId);
    }

    @Test
    void testDeleteComment_WithNonExistentComment_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Long nonExistentCommentId = 999L;
        
        doThrow(new ResourceNotFoundException("Comment", nonExistentCommentId))
                .when(commentService).deleteComment(postId, nonExistentCommentId);
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.deleteComment(postId, nonExistentCommentId)
        );
        
        assertEquals("Comment not found with id: 999", exception.getMessage());
        verify(commentService, times(1)).deleteComment(postId, nonExistentCommentId);
    }

    @Test
    void testDeleteComment_WithCommentNotBelongingToPost_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Long commentId = 1L;
        
        // Comment exists but belongs to a different post
        doThrow(new ResourceNotFoundException("Comment", commentId))
                .when(commentService).deleteComment(postId, commentId);
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> commentController.deleteComment(postId, commentId)
        );
        
        assertEquals("Comment not found with id: 1", exception.getMessage());
        verify(commentService, times(1)).deleteComment(postId, commentId);
    }
}
