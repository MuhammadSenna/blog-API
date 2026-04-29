package com.example.blog.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for method security enforcement.
 * 
 * Tests verify that @PreAuthorize annotations on controller methods are properly enforced:
 * - Write operations (POST, PUT, DELETE) require authentication
 * - Read operations (GET) are publicly accessible
 * 
 * Validates Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8
 */
@SpringBootTest
class MethodSecurityTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Test that @EnableMethodSecurity is configured on SecurityConfig
     */
    @Test
    void testSecurityConfig_HasEnableMethodSecurity() throws Exception {
        // Verify that SecurityConfig class has @EnableMethodSecurity annotation
        Class<?> securityConfigClass = Class.forName("com.example.blog.security.SecurityConfig");
        
        // Check if the class has the EnableMethodSecurity annotation
        boolean hasAnnotation = securityConfigClass.isAnnotationPresent(
                org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class);
        
        assertTrue(hasAnnotation, 
                "SecurityConfig should have @EnableMethodSecurity annotation");
    }

    // ========== Post Controller Security Tests ==========

    /**
     * Test that POST /api/posts has @PreAuthorize annotation
     * Validates Requirement 5.1
     */
    @Test
    void testCreatePost_HasPreAuthorizeAnnotation() throws Exception {
        Class<?> postControllerClass = Class.forName("com.example.blog.controller.PostController");
        Method createPostMethod = postControllerClass.getDeclaredMethod("createPost", 
                Class.forName("com.example.blog.dto.CreatePostRequest"));
        
        PreAuthorize preAuthorize = createPostMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "createPost method should have @PreAuthorize annotation");
        assertEquals("isAuthenticated()", preAuthorize.value(), 
                "createPost should require authentication");
    }

    /**
     * Test that PUT /api/posts/{id} has @PreAuthorize annotation
     * Validates Requirement 5.2
     */
    @Test
    void testUpdatePost_HasPreAuthorizeAnnotation() throws Exception {
        Class<?> postControllerClass = Class.forName("com.example.blog.controller.PostController");
        Method updatePostMethod = postControllerClass.getDeclaredMethod("updatePost", 
                Long.class, Class.forName("com.example.blog.dto.UpdatePostRequest"));
        
        PreAuthorize preAuthorize = updatePostMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "updatePost method should have @PreAuthorize annotation");
        assertEquals("isAuthenticated()", preAuthorize.value(), 
                "updatePost should require authentication");
    }

    /**
     * Test that DELETE /api/posts/{id} has @PreAuthorize annotation
     * Validates Requirement 5.3
     */
    @Test
    void testDeletePost_HasPreAuthorizeAnnotation() throws Exception {
        Class<?> postControllerClass = Class.forName("com.example.blog.controller.PostController");
        Method deletePostMethod = postControllerClass.getDeclaredMethod("deletePost", Long.class);
        
        PreAuthorize preAuthorize = deletePostMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "deletePost method should have @PreAuthorize annotation");
        assertEquals("isAuthenticated()", preAuthorize.value(), 
                "deletePost should require authentication");
    }

    /**
     * Test that GET /api/posts does NOT have @PreAuthorize annotation
     * Validates Requirement 5.7
     */
    @Test
    void testGetAllPosts_DoesNotHavePreAuthorizeAnnotation() throws Exception {
        Class<?> postControllerClass = Class.forName("com.example.blog.controller.PostController");
        Method getAllPostsMethod = postControllerClass.getDeclaredMethod("getAllPosts", 
                org.springframework.data.domain.Pageable.class);
        
        PreAuthorize preAuthorize = getAllPostsMethod.getAnnotation(PreAuthorize.class);
        assertNull(preAuthorize, "getAllPosts method should NOT have @PreAuthorize annotation");
    }

    /**
     * Test that GET /api/posts/{id} does NOT have @PreAuthorize annotation
     * Validates Requirement 5.7
     */
    @Test
    void testGetPostById_DoesNotHavePreAuthorizeAnnotation() throws Exception {
        Class<?> postControllerClass = Class.forName("com.example.blog.controller.PostController");
        Method getPostByIdMethod = postControllerClass.getDeclaredMethod("getPostById", Long.class);
        
        PreAuthorize preAuthorize = getPostByIdMethod.getAnnotation(PreAuthorize.class);
        assertNull(preAuthorize, "getPostById method should NOT have @PreAuthorize annotation");
    }

    // ========== Comment Controller Security Tests ==========

    /**
     * Test that POST /api/posts/{postId}/comments has @PreAuthorize annotation
     * Validates Requirement 5.4
     */
    @Test
    void testCreateComment_HasPreAuthorizeAnnotation() throws Exception {
        Class<?> commentControllerClass = Class.forName("com.example.blog.controller.CommentController");
        Method createCommentMethod = commentControllerClass.getDeclaredMethod("createComment", 
                Long.class, Class.forName("com.example.blog.dto.CreateCommentRequest"));
        
        PreAuthorize preAuthorize = createCommentMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "createComment method should have @PreAuthorize annotation");
        assertEquals("isAuthenticated()", preAuthorize.value(), 
                "createComment should require authentication");
    }

    /**
     * Test that PUT /api/posts/{postId}/comments/{commentId} has @PreAuthorize annotation
     * Validates Requirement 5.5
     */
    @Test
    void testUpdateComment_HasPreAuthorizeAnnotation() throws Exception {
        Class<?> commentControllerClass = Class.forName("com.example.blog.controller.CommentController");
        Method updateCommentMethod = commentControllerClass.getDeclaredMethod("updateComment", 
                Long.class, Long.class, Class.forName("com.example.blog.dto.UpdateCommentRequest"));
        
        PreAuthorize preAuthorize = updateCommentMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "updateComment method should have @PreAuthorize annotation");
        assertEquals("isAuthenticated()", preAuthorize.value(), 
                "updateComment should require authentication");
    }

    /**
     * Test that DELETE /api/posts/{postId}/comments/{commentId} has @PreAuthorize annotation
     * Validates Requirement 5.6
     */
    @Test
    void testDeleteComment_HasPreAuthorizeAnnotation() throws Exception {
        Class<?> commentControllerClass = Class.forName("com.example.blog.controller.CommentController");
        Method deleteCommentMethod = commentControllerClass.getDeclaredMethod("deleteComment", 
                Long.class, Long.class);
        
        PreAuthorize preAuthorize = deleteCommentMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "deleteComment method should have @PreAuthorize annotation");
        assertEquals("isAuthenticated()", preAuthorize.value(), 
                "deleteComment should require authentication");
    }

    /**
     * Test that GET /api/posts/{postId}/comments does NOT have @PreAuthorize annotation
     * Validates Requirement 5.8
     */
    @Test
    void testGetCommentsByPostId_DoesNotHavePreAuthorizeAnnotation() throws Exception {
        Class<?> commentControllerClass = Class.forName("com.example.blog.controller.CommentController");
        Method getCommentsByPostIdMethod = commentControllerClass.getDeclaredMethod("getCommentsByPostId", 
                Long.class);
        
        PreAuthorize preAuthorize = getCommentsByPostIdMethod.getAnnotation(PreAuthorize.class);
        assertNull(preAuthorize, "getCommentsByPostId method should NOT have @PreAuthorize annotation");
    }

    /**
     * Test that GET /api/posts/{postId}/comments/{commentId} does NOT have @PreAuthorize annotation
     * Validates Requirement 5.8
     */
    @Test
    void testGetCommentById_DoesNotHavePreAuthorizeAnnotation() throws Exception {
        Class<?> commentControllerClass = Class.forName("com.example.blog.controller.CommentController");
        Method getCommentByIdMethod = commentControllerClass.getDeclaredMethod("getCommentById", 
                Long.class, Long.class);
        
        PreAuthorize preAuthorize = getCommentByIdMethod.getAnnotation(PreAuthorize.class);
        assertNull(preAuthorize, "getCommentById method should NOT have @PreAuthorize annotation");
    }
}
