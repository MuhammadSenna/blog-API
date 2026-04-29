package com.example.blog.service;

import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.PageResponse;
import com.example.blog.dto.PostDTO;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostService
 * Tests business logic in isolation using Mockito mocks
 *
 * Validates Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8,
 *                         6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10, 6.11
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Category testCategory;
    private Tag testTag;
    private Post testPost;
    private CreatePostRequest createRequest;
    private UpdatePostRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Technology");

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setTags(new HashSet<>(Set.of(testTag)));
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreatePostRequest();
        createRequest.setTitle("Test Post");
        createRequest.setContent("Test Content");
        createRequest.setCategoryId(1L);
        createRequest.setTagIds(new HashSet<>(Set.of(1L)));

        updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Updated Post");
        updateRequest.setContent("Updated Content");
        updateRequest.setCategoryId(1L);
        updateRequest.setTagIds(new HashSet<>(Set.of(1L)));
    }

    // ==================== createPost tests ====================

    @Test
    void createPost_WithValidData_ReturnsPostDTO() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(tagRepository.findAllById(any())).thenReturn(List.of(testTag));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        PostDTO result = postService.createPost(createRequest, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());
        assertEquals("Technology", result.getCategoryName());
        assertTrue(result.getTagNames().contains("Java"));
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createPost_WithNonExistentUser_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> postService.createPost(createRequest, "nonexistent"));
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_WithNonExistentCategory_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> postService.createPost(createRequest, "testuser"));
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_WithNonExistentTag_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        // Return empty list - fewer tags than requested
        when(tagRepository.findAllById(any())).thenReturn(List.of());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> postService.createPost(createRequest, "testuser"));
        verify(postRepository, never()).save(any());
    }

    // ==================== updatePost tests ====================

    @Test
    void updatePost_WithValidData_ReturnsUpdatedPostDTO() {
        // Arrange
        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Updated Post");
        updatedPost.setContent("Updated Content");
        updatedPost.setUser(testUser);
        updatedPost.setCategory(testCategory);
        updatedPost.setTags(new HashSet<>(Set.of(testTag)));
        updatedPost.setCreatedAt(LocalDateTime.now());
        updatedPost.setUpdatedAt(LocalDateTime.now());

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(tagRepository.findAllById(any())).thenReturn(List.of(testTag));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        // Act
        PostDTO result = postService.updatePost(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Post", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> postService.updatePost(99L, updateRequest));
        verify(postRepository, never()).save(any());
    }

    // ==================== getPostById tests ====================

    @Test
    void getPostById_WithExistingPost_ReturnsPostDTO() {
        // Arrange
        when(postRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testPost));

        // Act
        PostDTO result = postService.getPostById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Post", result.getTitle());
        assertEquals("testuser", result.getAuthorUsername());
    }

    @Test
    void getPostById_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        when(postRepository.findByIdWithRelations(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> postService.getPostById(99L));
    }

    // ==================== getAllPosts tests ====================

    @Test
    void getAllPosts_ReturnsPageResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

        when(postRepository.findAll(pageable)).thenReturn(postPage);

        // Act
        PageResponse<PostDTO> result = postService.getAllPosts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
    }

    // ==================== deletePost tests ====================

    @Test
    void deletePost_WithExistingPost_DeletesSuccessfully() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postRepository).delete(testPost);

        // Act
        postService.deletePost(1L);

        // Assert
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    void deletePost_WithNonExistentPost_ThrowsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> postService.deletePost(99L));
        verify(postRepository, never()).delete(any());
    }
}
