package com.example.blog.service;

import com.example.blog.dto.CommentDTO;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService
 * Tests business logic in isolation using Mockito mocks
 *
 * Validates Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7,
 *                         7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 7.12
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private CreateCommentRequest createRequest;
    private UpdateCommentRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setUser(testUser);
        testPost.setTags(new HashSet<>());

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test Comment");
        testComment.setPost(testPost);
        testComment.setUser(testUser);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateCommentRequest();
        createRequest.setContent("Test Comment");

        updateRequest = new UpdateCommentRequest();
        updateRequest.setContent("Updated Comment");
    }

    // ==================== createComment tests ====================

    @Test
    void createComment_WithValidData_ReturnsCommentDTO() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        CommentDTO result = commentService.createComment(1L, createRequest, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Comment", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(99L, createRequest, "testuser"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_WithNonExistentUser_ThrowsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(1L, createRequest, "nonexistent"));
        verify(commentRepository, never()).save(any());
    }

    // ==================== updateComment tests ====================

    @Test
    void updateComment_WithValidData_ReturnsUpdatedCommentDTO() {
        // Arrange
        Comment updatedComment = new Comment();
        updatedComment.setId(1L);
        updatedComment.setContent("Updated Comment");
        updatedComment.setPost(testPost);
        updatedComment.setUser(testUser);
        updatedComment.setCreatedAt(LocalDateTime.now());
        updatedComment.setUpdatedAt(LocalDateTime.now());

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

        // Act
        CommentDTO result = commentService.updateComment(1L, 1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Comment", result.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void updateComment_WithNonExistentComment_ThrowsResourceNotFoundException() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(1L, 99L, updateRequest));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_WithCommentNotBelongingToPost_ThrowsResourceNotFoundException() {
        // Arrange - comment belongs to post 2, but we're updating for post 1
        Post otherPost = new Post();
        otherPost.setId(2L);
        otherPost.setTitle("Other Post");
        otherPost.setContent("Other Content");
        otherPost.setUser(testUser);
        otherPost.setTags(new HashSet<>());

        Comment commentOnOtherPost = new Comment();
        commentOnOtherPost.setId(1L);
        commentOnOtherPost.setContent("Comment on other post");
        commentOnOtherPost.setPost(otherPost);
        commentOnOtherPost.setUser(testUser);

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(commentOnOtherPost));

        // Act & Assert - trying to update comment for post 1, but comment belongs to post 2
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(1L, 1L, updateRequest));
        verify(commentRepository, never()).save(any());
    }

    // ==================== getCommentById tests ====================

    @Test
    void getCommentById_WithExistingComment_ReturnsCommentDTO() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));

        // Act
        CommentDTO result = commentService.getCommentById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Comment", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());
    }

    @Test
    void getCommentById_WithNonExistentComment_ThrowsResourceNotFoundException() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentById(1L, 99L));
    }

    // ==================== getCommentsByPostId tests ====================

    @Test
    void getCommentsByPostId_WithExistingPost_ReturnsCommentList() {
        // Arrange
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Second Comment");
        comment2.setPost(testPost);
        comment2.setUser(testUser);
        comment2.setCreatedAt(LocalDateTime.now());
        comment2.setUpdatedAt(LocalDateTime.now());

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostId(1L)).thenReturn(Arrays.asList(testComment, comment2));

        // Act
        List<CommentDTO> result = commentService.getCommentsByPostId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Comment", result.get(0).getContent());
        assertEquals("Second Comment", result.get(1).getContent());
    }

    @Test
    void getCommentsByPostId_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsByPostId(99L));
        verify(commentRepository, never()).findByPostId(any());
    }

    // ==================== deleteComment tests ====================

    @Test
    void deleteComment_WithExistingComment_DeletesSuccessfully() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));
        doNothing().when(commentRepository).delete(testComment);

        // Act
        commentService.deleteComment(1L, 1L);

        // Assert
        verify(commentRepository, times(1)).findByIdWithAuthor(1L);
        verify(commentRepository, times(1)).delete(testComment);
    }

    @Test
    void deleteComment_WithNonExistentComment_ThrowsResourceNotFoundException() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(1L, 99L));
        verify(commentRepository, never()).delete(any());
    }
}
