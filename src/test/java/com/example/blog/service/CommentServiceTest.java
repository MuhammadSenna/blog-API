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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService
 * Tests CRUD operations and business logic for comments
 * 
 * Validates Requirements: 9.4, 9.7
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

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Create test post
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setUser(testUser);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        // Create test comment
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test Comment");
        testComment.setPost(testPost);
        testComment.setUser(testUser);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createComment_Success() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New Comment");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        CommentDTO result = commentService.createComment(1L, request, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Comment", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());

        verify(postRepository).findById(1L);
        verify(userRepository).findByUsername("testuser");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_PostNotFound_ThrowsException() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New Comment");

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.createComment(999L, request, "testuser"));

        verify(postRepository).findById(999L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_UserNotFound_ThrowsException() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New Comment");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.createComment(1L, request, "nonexistent"));

        verify(postRepository).findById(1L);
        verify(userRepository).findByUsername("nonexistent");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_Success() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated Comment");

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        CommentDTO result = commentService.updateComment(1L, 1L, request);

        // Assert
        assertNotNull(result);
        verify(commentRepository).findByIdWithAuthor(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_CommentNotFound_ThrowsException() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated Comment");

        when(commentRepository.findByIdWithAuthor(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.updateComment(1L, 999L, request));

        verify(commentRepository).findByIdWithAuthor(999L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_CommentDoesNotBelongToPost_ThrowsException() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated Comment");

        Post differentPost = new Post();
        differentPost.setId(2L);
        testComment.setPost(differentPost);

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.updateComment(1L, 1L, request));

        verify(commentRepository).findByIdWithAuthor(1L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_PreservesAuthor() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated Comment");

        Comment originalComment = new Comment();
        originalComment.setId(1L);
        originalComment.setContent("Original Content");
        originalComment.setPost(testPost);
        originalComment.setUser(testUser);

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(originalComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        commentService.updateComment(1L, 1L, request);

        // Assert
        verify(commentRepository).save(argThat(comment ->
            comment.getUser().equals(testUser) &&
            comment.getContent().equals("Updated Comment")
        ));
    }

    @Test
    void getCommentById_Success() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));

        // Act
        CommentDTO result = commentService.getCommentById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Comment", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());

        verify(commentRepository).findByIdWithAuthor(1L);
    }

    @Test
    void getCommentById_CommentNotFound_ThrowsException() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.getCommentById(1L, 999L));

        verify(commentRepository).findByIdWithAuthor(999L);
    }

    @Test
    void getCommentById_CommentDoesNotBelongToPost_ThrowsException() {
        // Arrange
        Post differentPost = new Post();
        differentPost.setId(2L);
        testComment.setPost(differentPost);

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.getCommentById(1L, 1L));

        verify(commentRepository).findByIdWithAuthor(1L);
    }

    @Test
    void getCommentsByPostId_Success() {
        // Arrange
        List<Comment> comments = Arrays.asList(testComment);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostId(1L)).thenReturn(comments);

        // Act
        List<CommentDTO> result = commentService.getCommentsByPostId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getContent());
        assertEquals("testuser", result.get(0).getAuthorUsername());

        verify(postRepository).findById(1L);
        verify(commentRepository).findByPostId(1L);
    }

    @Test
    void getCommentsByPostId_PostNotFound_ThrowsException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.getCommentsByPostId(999L));

        verify(postRepository).findById(999L);
        verify(commentRepository, never()).findByPostId(any());
    }

    @Test
    void getCommentsByPostId_EmptyResult() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<CommentDTO> result = commentService.getCommentsByPostId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(postRepository).findById(1L);
        verify(commentRepository).findByPostId(1L);
    }

    @Test
    void deleteComment_Success() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));
        doNothing().when(commentRepository).delete(any(Comment.class));

        // Act
        commentService.deleteComment(1L, 1L);

        // Assert
        verify(commentRepository).findByIdWithAuthor(1L);
        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_CommentNotFound_ThrowsException() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.deleteComment(1L, 999L));

        verify(commentRepository).findByIdWithAuthor(999L);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_CommentDoesNotBelongToPost_ThrowsException() {
        // Arrange
        Post differentPost = new Post();
        differentPost.setId(2L);
        testComment.setPost(differentPost);

        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> commentService.deleteComment(1L, 1L));

        verify(commentRepository).findByIdWithAuthor(1L);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void convertToDTO_IncludesAllFields() {
        // Arrange
        when(commentRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(testComment));

        // Act
        CommentDTO result = commentService.getCommentById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getContent(), result.getContent());
        assertEquals(testComment.getUser().getUsername(), result.getAuthorUsername());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }
}
