package com.example.blog.controller;

import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.PageResponse;
import com.example.blog.dto.PostDTO;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostController
 * Tests GET endpoints for retrieving posts
 * 
 * Validates Requirements: 3.1, 3.2, 5.7, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    private PostController postController;

    @BeforeEach
    void setUp() {
        postController = new PostController(postService);
    }

    @Test
    void testControllerCreation_WithPostService_Success() {
        // Verify that the controller can be instantiated with PostService dependency
        assertNotNull(postController);
    }

    @Test
    void testGetAllPosts_WithDefaultPagination_ReturnsPageResponse() {
        // Arrange
        PostDTO post1 = createTestPostDTO(1L, "First Post", "Content 1");
        PostDTO post2 = createTestPostDTO(2L, "Second Post", "Content 2");
        List<PostDTO> posts = Arrays.asList(post1, post2);
        
        PageResponse<PostDTO> expectedResponse = new PageResponse<>(
                posts,
                0,      // pageNumber
                10,     // pageSize
                2L,     // totalElements
                1,      // totalPages
                true,   // isFirst
                true    // isLast
        );
        
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(expectedResponse);
        
        // Act
        ResponseEntity<PageResponse<PostDTO>> response = postController.getAllPosts(pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(0, response.getBody().getPageNumber());
        assertEquals(10, response.getBody().getPageSize());
        assertEquals(2L, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());
        assertTrue(response.getBody().isFirst());
        assertTrue(response.getBody().isLast());
        
        verify(postService, times(1)).getAllPosts(any(Pageable.class));
    }

    @Test
    void testGetAllPosts_WithCustomPagination_ReturnsPageResponse() {
        // Arrange
        PostDTO post1 = createTestPostDTO(1L, "First Post", "Content 1");
        List<PostDTO> posts = Arrays.asList(post1);
        
        PageResponse<PostDTO> expectedResponse = new PageResponse<>(
                posts,
                1,      // pageNumber (second page)
                5,      // pageSize
                10L,    // totalElements
                2,      // totalPages
                false,  // isFirst
                true    // isLast
        );
        
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(expectedResponse);
        
        // Act
        ResponseEntity<PageResponse<PostDTO>> response = postController.getAllPosts(pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1, response.getBody().getPageNumber());
        assertEquals(5, response.getBody().getPageSize());
        assertEquals(10L, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getTotalPages());
        assertFalse(response.getBody().isFirst());
        assertTrue(response.getBody().isLast());
        
        verify(postService, times(1)).getAllPosts(any(Pageable.class));
    }

    @Test
    void testGetAllPosts_WithEmptyResult_ReturnsEmptyPageResponse() {
        // Arrange
        List<PostDTO> emptyPosts = Arrays.asList();
        
        PageResponse<PostDTO> expectedResponse = new PageResponse<>(
                emptyPosts,
                0,      // pageNumber
                10,     // pageSize
                0L,     // totalElements
                0,      // totalPages
                true,   // isFirst
                true    // isLast
        );
        
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(expectedResponse);
        
        // Act
        ResponseEntity<PageResponse<PostDTO>> response = postController.getAllPosts(pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());
        assertEquals(0L, response.getBody().getTotalElements());
        
        verify(postService, times(1)).getAllPosts(any(Pageable.class));
    }

    @Test
    void testGetPostById_WithExistingId_ReturnsPost() {
        // Arrange
        Long postId = 1L;
        PostDTO expectedPost = createTestPostDTO(postId, "Test Post", "Test Content");
        
        when(postService.getPostById(postId)).thenReturn(expectedPost);
        
        // Act
        ResponseEntity<PostDTO> response = postController.getPostById(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(postId, response.getBody().getId());
        assertEquals("Test Post", response.getBody().getTitle());
        assertEquals("Test Content", response.getBody().getContent());
        assertEquals("testuser", response.getBody().getAuthorUsername());
        assertEquals("Technology", response.getBody().getCategoryName());
        
        verify(postService, times(1)).getPostById(postId);
    }

    @Test
    void testGetPostById_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        
        when(postService.getPostById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Post", nonExistentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postController.getPostById(nonExistentId)
        );
        
        assertEquals("Post not found with id: 999", exception.getMessage());
        assertEquals("Post", exception.getResourceType());
        assertEquals(nonExistentId, exception.getResourceId());
        
        verify(postService, times(1)).getPostById(nonExistentId);
    }

    @Test
    void testGetPostById_WithDifferentPost_ReturnsCorrectPost() {
        // Arrange
        Long postId = 42L;
        PostDTO expectedPost = createTestPostDTO(postId, "Another Post", "Another Content");
        
        when(postService.getPostById(postId)).thenReturn(expectedPost);
        
        // Act
        ResponseEntity<PostDTO> response = postController.getPostById(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(postId, response.getBody().getId());
        assertEquals("Another Post", response.getBody().getTitle());
        assertEquals("Another Content", response.getBody().getContent());
        
        verify(postService, times(1)).getPostById(postId);
    }

    /**
     * Helper method to create a test PostDTO
     */
    private PostDTO createTestPostDTO(Long id, String title, String content) {
        Set<String> tagNames = new HashSet<>();
        tagNames.add("java");
        tagNames.add("spring");
        
        return new PostDTO(
                id,
                title,
                content,
                "testuser",
                "Technology",
                tagNames,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
    }

    /**
     * Helper method to create a test CreatePostRequest
     */
    private CreatePostRequest createTestCreatePostRequest(String title, String content, Long categoryId, Set<Long> tagIds) {
        return new CreatePostRequest(title, content, categoryId, tagIds);
    }

    /**
     * Helper method to create a test UpdatePostRequest
     */
    private UpdatePostRequest createTestUpdatePostRequest(String title, String content, Long categoryId, Set<Long> tagIds) {
        return new UpdatePostRequest(title, content, categoryId, tagIds);
    }

    /**
     * Helper method to setup mock authentication context
     */
    private void setupMockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    // ========== Tests for POST /api/posts (Create Post) ==========

    @Test
    void testCreatePost_WithValidRequest_ReturnsCreatedPost() {
        // Arrange
        String username = "testuser";
        setupMockAuthentication(username);
        
        Set<Long> tagIds = new HashSet<>(Arrays.asList(1L, 2L));
        CreatePostRequest request = createTestCreatePostRequest("New Post", "New Content", 1L, tagIds);
        PostDTO expectedPost = createTestPostDTO(1L, "New Post", "New Content");
        
        when(postService.createPost(any(CreatePostRequest.class), eq(username))).thenReturn(expectedPost);
        
        // Act
        ResponseEntity<PostDTO> response = postController.createPost(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Post", response.getBody().getTitle());
        assertEquals("New Content", response.getBody().getContent());
        
        verify(postService, times(1)).createPost(any(CreatePostRequest.class), eq(username));
    }

    @Test
    void testCreatePost_WithCategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String username = "testuser";
        setupMockAuthentication(username);
        
        Set<Long> tagIds = new HashSet<>(Arrays.asList(1L, 2L));
        CreatePostRequest request = createTestCreatePostRequest("New Post", "New Content", 999L, tagIds);
        
        when(postService.createPost(any(CreatePostRequest.class), eq(username)))
                .thenThrow(new ResourceNotFoundException("Category", 999L));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postController.createPost(request)
        );
        
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(postService, times(1)).createPost(any(CreatePostRequest.class), eq(username));
    }

    @Test
    void testCreatePost_WithTagNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String username = "testuser";
        setupMockAuthentication(username);
        
        Set<Long> tagIds = new HashSet<>(Arrays.asList(1L, 999L));
        CreatePostRequest request = createTestCreatePostRequest("New Post", "New Content", 1L, tagIds);
        
        when(postService.createPost(any(CreatePostRequest.class), eq(username)))
                .thenThrow(new ResourceNotFoundException("Tag", 0L));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postController.createPost(request)
        );
        
        assertTrue(exception.getMessage().contains("Tag not found"));
        verify(postService, times(1)).createPost(any(CreatePostRequest.class), eq(username));
    }

    // ========== Tests for PUT /api/posts/{id} (Update Post) ==========

    @Test
    void testUpdatePost_WithValidRequest_ReturnsUpdatedPost() {
        // Arrange
        Long postId = 1L;
        Set<Long> tagIds = new HashSet<>(Arrays.asList(1L, 2L));
        UpdatePostRequest request = createTestUpdatePostRequest("Updated Title", "Updated Content", 1L, tagIds);
        PostDTO expectedPost = createTestPostDTO(postId, "Updated Title", "Updated Content");
        
        when(postService.updatePost(eq(postId), any(UpdatePostRequest.class))).thenReturn(expectedPost);
        
        // Act
        ResponseEntity<PostDTO> response = postController.updatePost(postId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getTitle());
        assertEquals("Updated Content", response.getBody().getContent());
        
        verify(postService, times(1)).updatePost(eq(postId), any(UpdatePostRequest.class));
    }

    @Test
    void testUpdatePost_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        Set<Long> tagIds = new HashSet<>(Arrays.asList(1L, 2L));
        UpdatePostRequest request = createTestUpdatePostRequest("Updated Title", "Updated Content", 1L, tagIds);
        
        when(postService.updatePost(eq(nonExistentId), any(UpdatePostRequest.class)))
                .thenThrow(new ResourceNotFoundException("Post", nonExistentId));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postController.updatePost(nonExistentId, request)
        );
        
        assertEquals("Post not found with id: 999", exception.getMessage());
        verify(postService, times(1)).updatePost(eq(nonExistentId), any(UpdatePostRequest.class));
    }

    @Test
    void testUpdatePost_WithInvalidCategory_ThrowsResourceNotFoundException() {
        // Arrange
        Long postId = 1L;
        Set<Long> tagIds = new HashSet<>(Arrays.asList(1L, 2L));
        UpdatePostRequest request = createTestUpdatePostRequest("Updated Title", "Updated Content", 999L, tagIds);
        
        when(postService.updatePost(eq(postId), any(UpdatePostRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category", 999L));
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postController.updatePost(postId, request)
        );
        
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(postService, times(1)).updatePost(eq(postId), any(UpdatePostRequest.class));
    }

    // ========== Tests for DELETE /api/posts/{id} (Delete Post) ==========

    @Test
    void testDeletePost_WithExistingId_ReturnsNoContent() {
        // Arrange
        Long postId = 1L;
        doNothing().when(postService).deletePost(postId);
        
        // Act
        ResponseEntity<Void> response = postController.deletePost(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(postService, times(1)).deletePost(postId);
    }

    @Test
    void testDeletePost_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        doThrow(new ResourceNotFoundException("Post", nonExistentId))
                .when(postService).deletePost(nonExistentId);
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postController.deletePost(nonExistentId)
        );
        
        assertEquals("Post not found with id: 999", exception.getMessage());
        verify(postService, times(1)).deletePost(nonExistentId);
    }

    @Test
    void testDeletePost_WithDifferentId_DeletesCorrectPost() {
        // Arrange
        Long postId = 42L;
        doNothing().when(postService).deletePost(postId);
        
        // Act
        ResponseEntity<Void> response = postController.deletePost(postId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        verify(postService, times(1)).deletePost(postId);
    }
}
