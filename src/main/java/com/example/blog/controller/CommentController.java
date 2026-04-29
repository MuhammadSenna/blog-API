package com.example.blog.controller;

import com.example.blog.dto.CommentDTO;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.ErrorResponse;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for comment operations on blog posts
 * Handles HTTP requests for CRUD operations on comments
 * 
 * Validates Requirements: 4.1, 4.2, 5.8
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * Constructor injection of CommentService dependency
     * 
     * @param commentService the comment service for business logic
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Retrieves all comments for a specific post
     * 
     * @param postId the ID of the post to retrieve comments for
     * @return ResponseEntity with List of CommentDTO
     * @throws ResourceNotFoundException if post is not found (handled by GlobalExceptionHandler)
     * 
     * Validates Requirements: 4.1, 5.8
     */
    @Operation(
        summary = "Get all comments for a post",
        description = "Retrieves all comments for a specific blog post. Returns a list of comments with author information. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of comments",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentDTO.class)
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
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(
            @Parameter(
                description = "Unique identifier of the blog post",
                required = true,
                example = "1"
            )
            @PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Retrieves a single comment by ID
     * Validates that comment belongs to the specified post
     * 
     * @param postId the ID of the post the comment belongs to
     * @param commentId the ID of the comment to retrieve
     * @return ResponseEntity with CommentDTO
     * @throws ResourceNotFoundException if comment is not found or doesn't belong to the post
     * 
     * Validates Requirements: 4.2, 5.8
     */
    @Operation(
        summary = "Get a comment by ID",
        description = "Retrieves a single comment by its unique identifier. Validates that the comment belongs to the specified post. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the comment",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Comment not found or doesn't belong to the specified post",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(
            @Parameter(
                description = "Unique identifier of the blog post",
                required = true,
                example = "1"
            )
            @PathVariable Long postId,
            @Parameter(
                description = "Unique identifier of the comment",
                required = true,
                example = "1"
            )
            @PathVariable Long commentId) {
        CommentDTO comment = commentService.getCommentById(postId, commentId);
        return ResponseEntity.ok(comment);
    }

    /**
     * Creates a new comment on a post
     * Requires authentication - gets current username from SecurityContext
     * 
     * @param postId the ID of the post to comment on
     * @param request the create comment request containing comment data
     * @return ResponseEntity with created CommentDTO and HTTP 201 Created status
     * @throws ResourceNotFoundException if post is not found
     * 
     * Validates Requirements: 4.3, 4.6, 5.4
     */
    @Operation(
        summary = "Create a new comment on a post",
        description = "Creates a new comment on a blog post. Requires JWT authentication. The authenticated user will be set as the author of the comment.",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Comment created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentDTO.class)
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
            description = "Post not found with the given ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(
            @Parameter(
                description = "Unique identifier of the blog post to comment on",
                required = true,
                example = "1"
            )
            @PathVariable Long postId,
            @Parameter(
                description = "Request body containing comment content",
                required = true
            )
            @Valid @RequestBody CreateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CommentDTO createdComment = commentService.createComment(postId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * Updates an existing comment
     * Requires authentication
     * Validates that comment belongs to the specified post
     * 
     * @param postId the ID of the post the comment belongs to
     * @param commentId the ID of the comment to update
     * @param request the update comment request containing updated data
     * @return ResponseEntity with updated CommentDTO and HTTP 200 OK status
     * @throws ResourceNotFoundException if comment is not found or doesn't belong to the post
     * 
     * Validates Requirements: 4.4, 4.7, 5.5
     */
    @Operation(
        summary = "Update an existing comment",
        description = "Updates an existing comment on a blog post. Requires JWT authentication. Validates that the comment belongs to the specified post.",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comment updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentDTO.class)
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
            description = "Comment not found or doesn't belong to the specified post",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> updateComment(
            @Parameter(
                description = "Unique identifier of the blog post",
                required = true,
                example = "1"
            )
            @PathVariable Long postId,
            @Parameter(
                description = "Unique identifier of the comment to update",
                required = true,
                example = "1"
            )
            @PathVariable Long commentId,
            @Parameter(
                description = "Request body containing updated comment content",
                required = true
            )
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentDTO updatedComment = commentService.updateComment(postId, commentId, request);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * Deletes a comment
     * Requires authentication
     * Validates that comment belongs to the specified post
     * 
     * @param postId the ID of the post the comment belongs to
     * @param commentId the ID of the comment to delete
     * @return ResponseEntity with HTTP 204 No Content status
     * @throws ResourceNotFoundException if comment is not found or doesn't belong to the post
     * 
     * Validates Requirements: 4.5, 4.8, 5.6
     */
    @Operation(
        summary = "Delete a comment",
        description = "Deletes an existing comment from a blog post. Requires JWT authentication. Validates that the comment belongs to the specified post.",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Comment deleted successfully (no content returned)"
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
            description = "Comment not found or doesn't belong to the specified post",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @Parameter(
                description = "Unique identifier of the blog post",
                required = true,
                example = "1"
            )
            @PathVariable Long postId,
            @Parameter(
                description = "Unique identifier of the comment to delete",
                required = true,
                example = "1"
            )
            @PathVariable Long commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
