package com.example.blog.integration;

import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Category;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.CommentRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CommentController
 * Tests the complete comment management flow with real Spring context
 *
 * Validates Requirements: 8.1, 8.2, 8.10, 8.11
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerIntegrationTest {

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
    private CommentRepository commentRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = TestDataBuilder.createTestUser("testuser", "test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        Category testCategory = TestDataBuilder.createTestCategory("Technology");
        testCategory = categoryRepository.save(testCategory);

        testPost = TestDataBuilder.createTestPost("Test Post", "Test Content", testUser, testCategory);
        testPost = postRepository.save(testPost);

        jwtToken = jwtTokenProvider.generateToken(testUser.getUsername());
    }

    // ==================== createComment tests ====================

    @Test
    void createComment_WithValidToken_Returns201() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("This is a test comment");

        mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("This is a test comment"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));
    }

    @Test
    void createComment_WithoutToken_Returns401() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("This is a test comment");

        mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== getCommentsByPostId tests ====================

    @Test
    void getCommentsByPostId_Returns200WithCommentList() throws Exception {
        Comment comment1 = TestDataBuilder.createTestComment("First comment", testUser, testPost);
        Comment comment2 = TestDataBuilder.createTestComment("Second comment", testUser, testPost);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== getCommentById tests ====================

    @Test
    void getCommentById_WithExistingComment_Returns200() throws Exception {
        Comment comment = TestDataBuilder.createTestComment("Test comment", testUser, testPost);
        comment = commentRepository.save(comment);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}",
                testPost.getId(), comment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    // ==================== updateComment tests ====================

    @Test
    void updateComment_WithValidToken_Returns200() throws Exception {
        Comment comment = TestDataBuilder.createTestComment("Original comment", testUser, testPost);
        comment = commentRepository.save(comment);

        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated comment content");

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}",
                testPost.getId(), comment.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment content"));
    }

    // ==================== deleteComment tests ====================

    @Test
    void deleteComment_WithValidToken_Returns204() throws Exception {
        Comment comment = TestDataBuilder.createTestComment("Comment to delete", testUser, testPost);
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}",
                testPost.getId(), comment.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
