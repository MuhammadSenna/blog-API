package com.example.blog.controller;

import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.PageResponse;
import com.example.blog.dto.PostDTO;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for blog post operations
 * Handles HTTP requests for CRUD operations on posts
 * 
 * Validates Requirements: 3.1, 3.2, 5.7, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    /**
     * Constructor injection of PostService dependency
     * 
     * @param postService the post service for business logic
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Retrieves all posts with pagination and sorting
     * Default pagination: page 0, size 10, sorted by createdAt descending
     * 
     * @param pageable pagination information (page, size, sort)
     * @return ResponseEntity with PageResponse containing posts and pagination metadata
     * 
     * Validates Requirements: 3.1, 5.7, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostDTO>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PostDTO> response = postService.getAllPosts(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single post by ID
     * 
     * @param id the ID of the post to retrieve
     * @return ResponseEntity with PostDTO
     * @throws ResourceNotFoundException if post is not found (handled by GlobalExceptionHandler)
     * 
     * Validates Requirements: 3.2, 5.7
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * Creates a new blog post
     * Requires authentication - gets current username from SecurityContext
     * 
     * @param request the create post request containing post data
     * @return ResponseEntity with created PostDTO and 201 Created status
     * @throws ResourceNotFoundException if category or tags are not found
     * 
     * Validates Requirements: 3.3, 3.6, 5.1
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PostDTO createdPost = postService.createPost(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    /**
     * Updates an existing blog post
     * Requires authentication
     * 
     * @param id the ID of the post to update
     * @param request the update post request containing updated data
     * @return ResponseEntity with updated PostDTO and 200 OK status
     * @throws ResourceNotFoundException if post, category, or tags are not found
     * 
     * Validates Requirements: 3.4, 3.7, 5.2
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        PostDTO updatedPost = postService.updatePost(id, request);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Deletes a blog post
     * Requires authentication
     * 
     * @param id the ID of the post to delete
     * @return ResponseEntity with 204 No Content status
     * @throws ResourceNotFoundException if post is not found
     * 
     * Validates Requirements: 3.5, 3.8, 5.3
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
