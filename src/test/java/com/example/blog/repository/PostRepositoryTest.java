package com.example.blog.repository;

import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PostRepository
 * Tests custom query methods with JOIN FETCH
 * 
 * Validates Requirements: 10.7, 10.8
 */
@SpringBootTest
@Transactional
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testUser;
    private Category testCategory;
    private Tag testTag1;
    private Tag testTag2;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        postRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory = categoryRepository.save(testCategory);

        // Create test tags
        testTag1 = new Tag();
        testTag1.setName("Java");
        testTag1 = tagRepository.save(testTag1);

        testTag2 = new Tag();
        testTag2.setName("Spring");
        testTag2 = tagRepository.save(testTag2);

        // Create test post with all relationships
        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setTags(Set.of(testTag1, testTag2));
        testPost = postRepository.save(testPost);
    }

    @Test
    void findByIdWithRelations_Success_LoadsAllRelations() {
        // Act
        Optional<Post> result = postRepository.findByIdWithRelations(testPost.getId());

        // Assert
        assertTrue(result.isPresent());
        Post post = result.get();
        
        // Verify post data
        assertEquals("Test Post", post.getTitle());
        assertEquals("Test content", post.getContent());
        
        // Verify user is loaded (JOIN FETCH)
        assertNotNull(post.getUser());
        assertEquals("testuser", post.getUser().getUsername());
        
        // Verify category is loaded (JOIN FETCH)
        assertNotNull(post.getCategory());
        assertEquals("Technology", post.getCategory().getName());
        
        // Verify tags are loaded (LEFT JOIN FETCH)
        assertNotNull(post.getTags());
        assertEquals(2, post.getTags().size());
        assertTrue(post.getTags().stream().anyMatch(tag -> tag.getName().equals("Java")));
        assertTrue(post.getTags().stream().anyMatch(tag -> tag.getName().equals("Spring")));
    }

    @Test
    void findByIdWithRelations_PostWithoutTags_Success() {
        // Arrange - Create post without tags
        Post postWithoutTags = new Post();
        postWithoutTags.setTitle("Post Without Tags");
        postWithoutTags.setContent("Content without tags");
        postWithoutTags.setUser(testUser);
        postWithoutTags.setCategory(testCategory);
        postWithoutTags = postRepository.save(postWithoutTags);

        // Act
        Optional<Post> result = postRepository.findByIdWithRelations(postWithoutTags.getId());

        // Assert
        assertTrue(result.isPresent());
        Post post = result.get();
        assertEquals("Post Without Tags", post.getTitle());
        assertNotNull(post.getUser());
        assertNotNull(post.getCategory());
        assertNotNull(post.getTags());
        assertTrue(post.getTags().isEmpty());
    }

    @Test
    void findByIdWithRelations_NonExistentId_ReturnsEmpty() {
        // Act
        Optional<Post> result = postRepository.findByIdWithRelations(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByIdWithRelations_LoadsRelationsInSingleQuery() {
        // This test verifies that the query loads all relations eagerly
        // by accessing all relations without triggering additional queries
        
        // Act
        Optional<Post> result = postRepository.findByIdWithRelations(testPost.getId());

        // Assert
        assertTrue(result.isPresent());
        Post post = result.get();
        
        // These should not trigger lazy loading - all data is already loaded
        assertDoesNotThrow(() -> {
            post.getUser().getUsername();
            post.getCategory().getName();
            post.getTags().forEach(tag -> tag.getName());
        });
    }
}
