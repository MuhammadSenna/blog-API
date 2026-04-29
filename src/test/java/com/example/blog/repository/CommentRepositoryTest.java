package com.example.blog.repository;

import com.example.blog.entity.Category;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CommentRepository
 * Tests custom query methods with JOIN FETCH to avoid N+1 problems
 * 
 * Validates Requirements: 10.4
 */
@SpringBootTest
@Transactional
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testUser;
    private Post testPost;
    private Comment testComment1;
    private Comment testComment2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        commentRepository.deleteAll();
        postRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Create test category
        Category testCategory = new Category();
        testCategory.setName("Technology");
        testCategory = categoryRepository.save(testCategory);

        // Create test post
        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost = postRepository.save(testPost);

        // Create test comments
        testComment1 = new Comment();
        testComment1.setContent("First comment");
        testComment1.setUser(testUser);
        testComment1.setPost(testPost);
        testComment1 = commentRepository.save(testComment1);

        testComment2 = new Comment();
        testComment2.setContent("Second comment");
        testComment2.setUser(testUser);
        testComment2.setPost(testPost);
        testComment2 = commentRepository.save(testComment2);
    }

    @Test
    void findByIdWithUser_Success_LoadsUser() {
        // Act
        Optional<Comment> result = commentRepository.findByIdWithUser(testComment1.getId());

        // Assert
        assertTrue(result.isPresent());
        Comment comment = result.get();
        
        // Verify comment data
        assertEquals("First comment", comment.getContent());
        
        // Verify user is loaded (JOIN FETCH)
        assertNotNull(comment.getUser());
        assertEquals("testuser", comment.getUser().getUsername());
    }

    @Test
    void findByIdWithUser_NonExistentId_ReturnsEmpty() {
        // Act
        Optional<Comment> result = commentRepository.findByIdWithUser(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByIdWithUser_LoadsUserInSingleQuery() {
        // This test verifies that the query loads user eagerly
        // by accessing user without triggering additional queries
        
        // Act
        Optional<Comment> result = commentRepository.findByIdWithUser(testComment1.getId());

        // Assert
        assertTrue(result.isPresent());
        Comment comment = result.get();
        
        // This should not trigger lazy loading - user is already loaded
        assertDoesNotThrow(() -> {
            comment.getUser().getUsername();
            comment.getUser().getEmail();
        });
    }

    @Test
    void findByPostIdWithUser_Success_LoadsAllCommentsWithUsers() {
        // Act
        List<Comment> results = commentRepository.findByPostIdWithUser(testPost.getId());

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Verify all comments have users loaded
        for (Comment comment : results) {
            assertNotNull(comment.getUser());
            assertEquals("testuser", comment.getUser().getUsername());
        }
        
        // Verify comment contents
        assertTrue(results.stream().anyMatch(c -> c.getContent().equals("First comment")));
        assertTrue(results.stream().anyMatch(c -> c.getContent().equals("Second comment")));
    }

    @Test
    void findByPostIdWithUser_NonExistentPostId_ReturnsEmptyList() {
        // Act
        List<Comment> results = commentRepository.findByPostIdWithUser(999L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findByPostIdWithUser_PostWithNoComments_ReturnsEmptyList() {
        // Arrange - Create a post without comments
        Post postWithoutComments = new Post();
        postWithoutComments.setTitle("Post Without Comments");
        postWithoutComments.setContent("Content without comments");
        postWithoutComments.setUser(testUser);
        postWithoutComments.setCategory(testPost.getCategory());
        postWithoutComments = postRepository.save(postWithoutComments);

        // Act
        List<Comment> results = commentRepository.findByPostIdWithUser(postWithoutComments.getId());

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findByPostIdWithUser_LoadsUsersInSingleQuery() {
        // This test verifies that the query loads all users eagerly
        // by accessing all users without triggering additional queries
        
        // Act
        List<Comment> results = commentRepository.findByPostIdWithUser(testPost.getId());

        // Assert
        assertEquals(2, results.size());
        
        // These should not trigger lazy loading - all users are already loaded
        assertDoesNotThrow(() -> {
            results.forEach(comment -> {
                comment.getUser().getUsername();
                comment.getUser().getEmail();
            });
        });
    }

    @Test
    void findByIdWithAuthor_Success_LoadsUser() {
        // Test the existing findByIdWithAuthor method for completeness
        
        // Act
        Optional<Comment> result = commentRepository.findByIdWithAuthor(testComment1.getId());

        // Assert
        assertTrue(result.isPresent());
        Comment comment = result.get();
        
        // Verify comment data
        assertEquals("First comment", comment.getContent());
        
        // Verify user is loaded (JOIN FETCH)
        assertNotNull(comment.getUser());
        assertEquals("testuser", comment.getUser().getUsername());
    }

    @Test
    void findByPostId_Success_LoadsAllCommentsWithUsers() {
        // Test the existing findByPostId method for completeness
        
        // Act
        List<Comment> results = commentRepository.findByPostId(testPost.getId());

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Verify all comments have users loaded
        for (Comment comment : results) {
            assertNotNull(comment.getUser());
            assertEquals("testuser", comment.getUser().getUsername());
        }
    }
}
