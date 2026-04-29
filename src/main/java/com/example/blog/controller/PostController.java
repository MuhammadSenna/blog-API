package com.example.blog.controller;

import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.ErrorResponse;
import com.example.blog.dto.PageResponse;
import com.example.blog.dto.PostDTO;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Get all blog posts",
        description = "Retrieves a paginated list of all blog posts sorted by creation date (newest first). " +
                      "Supports pagination and custom sorting. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved paginated list of posts",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<PageResponse<PostDTO>> getAllPosts(
            @Parameter(
                description = "Pagination parameters (page number, page size, sort field and direction). " +
                              "Default: page=0, size=10, sort=createdAt,desc",
                example = "page=0&size=10&sort=createdAt,desc"
            )
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
    @Operation(
        summary = "Get a blog post by ID",
        description = "Retrieves a single blog post by its unique identifier. Returns the complete post details including title, content, author, category, tags, and timestamps. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the post",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found with the given ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(
            @Parameter(
                description = "Unique identifier of the blog post to retrieve",
                required = true,
                example = "1"
            )
            @PathVariable Long id) {
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
    @Operation(
        summary = "Create a new blog post",
        description = "Creates a new blog post with title, content, category, and tags. Requires JWT authentication. " +
                      "The authenticated user will be set as the author of the post.",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Post created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.blog.dto.ValidationErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token missing or invalid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Category or tag not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> createPost(
            @Parameter(
                description = "Request body containing post details (title, content, categoryId, tagIds)",
                required = true
            )
            @Valid @RequestBody CreatePostRequest request) {
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
    @Operation(
        summary = "Update an existing blog post",
        description = "Updates an existing blog post with new title, content, category, and tags. Requires JWT authentication. " +
                      "Only the authenticated user who created the post can update it.",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.example.blog.dto.ValidationErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token missing or invalid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post, category, or tag not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> updatePost(
            @Parameter(
                description = "Unique identifier of the blog post to update",
                required = true,
                example = "1"
            )
            @PathVariable Long id,
            @Parameter(
                description = "Request body containing updated post details (title, content, categoryId, tagIds)",
                required = true
            )
            @Valid @RequestBody UpdatePostRequest request) {
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
    @Operation(
        summary = "Delete a blog post",
        description = "Deletes an existing blog post by its unique identifier. Requires JWT authentication. " +
                      "Only the authenticated user who created the post can delete it.",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Post deleted successfully (no content returned)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token missing or invalid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found with the given ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @Parameter(
                description = "Unique identifier of the blog post to delete",
                required = true,
                example = "1"
            )
            @PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
