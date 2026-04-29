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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostService
 * Tests CRUD operations and business logic
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
    private Tag testTag1;
    private Tag testTag2;
    private Post testPost;

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

        // Create test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Technology");

        // Create test tags
        testTag1 = new Tag();
        testTag1.setId(1L);
        testTag1.setName("Java");

        testTag2 = new Tag();
        testTag2.setId(2L);
        testTag2.setName("Spring");

        // Create test post
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setTags(new HashSet<>(Arrays.asList(testTag1, testTag2)));
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createPost_Success() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setCategoryId(1L);
        request.setTagIds(new HashSet<>(Arrays.asList(1L, 2L)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(tagRepository.findAllById(any())).thenReturn(Arrays.asList(testTag1, testTag2));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        PostDTO result = postService.createPost(request, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());
        assertEquals("Technology", result.getCategoryName());
        assertEquals(2, result.getTagNames().size());
        assertTrue(result.getTagNames().contains("Java"));
        assertTrue(result.getTagNames().contains("Spring"));

        verify(userRepository).findByUsername("testuser");
        verify(categoryRepository).findById(1L);
        verify(tagRepository).findAllById(any());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_UserNotFound_ThrowsException() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setCategoryId(1L);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> postService.createPost(request, "nonexistent"));

        verify(userRepository).findByUsername("nonexistent");
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_CategoryNotFound_ThrowsException() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setCategoryId(999L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> postService.createPost(request, "testuser"));

        verify(categoryRepository).findById(999L);
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_TagNotFound_ThrowsException() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setCategoryId(1L);
        request.setTagIds(new HashSet<>(Arrays.asList(1L, 999L)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(tagRepository.findAllById(any())).thenReturn(Arrays.asList(testTag1)); // Only 1 tag found

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> postService.createPost(request, "testuser"));

        verify(tagRepository).findAllById(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_Success() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Post");
        request.setContent("Updated Content");
        request.setCategoryId(1L);
        request.setTagIds(new HashSet<>(Arrays.asList(1L)));

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(tagRepository.findAllById(any())).thenReturn(Arrays.asList(testTag1));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        PostDTO result = postService.updatePost(1L, request);

        // Assert
        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(tagRepository).findAllById(any());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_PostNotFound_ThrowsException() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Post");
        request.setContent("Updated Content");
        request.setCategoryId(1L);

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> postService.updatePost(999L, request));

        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_PreservesAuthor() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Post");
        request.setContent("Updated Content");
        request.setCategoryId(1L);
        request.setTagIds(new HashSet<>());

        Post originalPost = new Post();
        originalPost.setId(1L);
        originalPost.setTitle("Original Title");
        originalPost.setContent("Original Content");
        originalPost.setUser(testUser);
        originalPost.setCategory(testCategory);
        originalPost.setTags(new HashSet<>());

        when(postRepository.findById(1L)).thenReturn(Optional.of(originalPost));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        postService.updatePost(1L, request);

        // Assert
        verify(postRepository).save(argThat(post -> 
            post.getUser().equals(testUser) && 
            post.getTitle().equals("Updated Post") &&
            post.getContent().equals("Updated Content")
        ));
    }

    @Test
    void getPostById_Success() {
        // Arrange
        when(postRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testPost));

        // Act
        PostDTO result = postService.getPostById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Post", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("testuser", result.getAuthorUsername());
        assertEquals("Technology", result.getCategoryName());

        verify(postRepository).findByIdWithRelations(1L);
    }

    @Test
    void getPostById_NotFound_ThrowsException() {
        // Arrange
        when(postRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> postService.getPostById(999L));

        verify(postRepository).findByIdWithRelations(999L);
    }

    @Test
    void getAllPosts_Success() {
        // Arrange
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);

        when(postRepository.findAll(any(Pageable.class))).thenReturn(postPage);

        // Act
        PageResponse<PostDTO> result = postService.getAllPosts(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(postRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllPosts_EmptyResult() {
        // Arrange
        Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(postRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PageResponse<PostDTO> result = postService.getAllPosts(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());

        verify(postRepository).findAll(any(Pageable.class));
    }

    @Test
    void deletePost_Success() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postRepository).delete(any(Post.class));

        // Act
        postService.deletePost(1L);

        // Assert
        verify(postRepository).findById(1L);
        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_NotFound_ThrowsException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> postService.deletePost(999L));

        verify(postRepository).findById(999L);
        verify(postRepository, never()).delete(any());
    }

    @Test
    void convertToDTO_WithNullCategory() {
        // Arrange
        testPost.setCategory(null);
        when(postRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testPost));

        // Act
        PostDTO result = postService.getPostById(1L);

        // Assert
        assertNotNull(result);
        assertNull(result.getCategoryName());
    }

    @Test
    void convertToDTO_WithEmptyTags() {
        // Arrange
        testPost.setTags(new HashSet<>());
        when(postRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testPost));

        // Act
        PostDTO result = postService.getPostById(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTagNames().isEmpty());
    }
}
