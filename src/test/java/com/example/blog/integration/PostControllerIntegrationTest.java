package com.example.blog.integration;

import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.JwtTokenProvider;
import com.example.blog.util.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PostController
 * Tests the complete post management flow with real Spring context
 *
 * Validates Requirements: 8.1, 8.2, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = TestDataBuilder.createTestUser("testuser", "test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        testCategory = TestDataBuilder.createTestCategory("Technology");
        testCategory = categoryRepository.save(testCategory);

        jwtToken = jwtTokenProvider.generateToken(testUser.getUsername());
    }

    // ==================== createPost tests ====================

    @Test
    void createPost_WithValidToken_Returns201() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Integration Test Post");
        request.setContent("Integration test content");
        request.setCategoryId(testCategory.getId());
        request.setTagIds(new HashSet<>());

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Post"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));
    }

    @Test
    void createPost_WithoutToken_Returns401() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Post");
        request.setContent("Test content");
        request.setCategoryId(testCategory.getId());
        request.setTagIds(new HashSet<>());

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_WithInvalidToken_Returns401() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Post");
        request.setContent("Test content");
        request.setCategoryId(testCategory.getId());
        request.setTagIds(new HashSet<>());

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer invalid.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== getAllPosts tests ====================

    @Test
    void getAllPosts_Returns200WithPaginatedResponse() throws Exception {
        Post post1 = TestDataBuilder.createTestPost("Post 1", "Content 1", testUser, testCategory);
        Post post2 = TestDataBuilder.createTestPost("Post 2", "Content 2", testUser, testCategory);
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    // ==================== getPostById tests ====================

    @Test
    void getPostById_WithExistingPost_Returns200() throws Exception {
        Post post = TestDataBuilder.createTestPost("Test Post", "Test Content", testUser, testCategory);
        post = postRepository.save(post);

        mockMvc.perform(get("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    void getPostById_WithNonExistentPost_Returns404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== updatePost tests ====================

    @Test
    void updatePost_WithValidToken_Returns200() throws Exception {
        Post post = TestDataBuilder.createTestPost("Original Title", "Original Content", testUser, testCategory);
        post = postRepository.save(post);

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setCategoryId(testCategory.getId());
        request.setTagIds(new HashSet<>());

        mockMvc.perform(put("/api/posts/{id}", post.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updatePost_WithoutToken_Returns401() throws Exception {
        Post post = TestDataBuilder.createTestPost("Test Post", "Test Content", testUser, testCategory);
        post = postRepository.save(post);

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setCategoryId(testCategory.getId());
        request.setTagIds(new HashSet<>());

        mockMvc.perform(put("/api/posts/{id}", post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== deletePost tests ====================

    @Test
    void deletePost_WithValidToken_Returns204() throws Exception {
        Post post = TestDataBuilder.createTestPost("Test Post", "Test Content", testUser, testCategory);
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/{id}", post.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePost_WithoutToken_Returns401() throws Exception {
        Post post = TestDataBuilder.createTestPost("Test Post", "Test Content", testUser, testCategory);
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isUnauthorized());
    }
}
