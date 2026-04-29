package com.example.blog.controller;

import com.example.blog.dto.CommentDTO;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.service.CommentService;
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
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
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
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(
            @PathVariable Long postId,
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
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long postId,
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
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
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
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
