package com.example.blog.service;

import com.example.blog.dto.CommentDTO;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing comments on blog posts
 * Handles business logic for CRUD operations on comments
 * 
 * Validates Requirements: 9.4, 9.7
 */
@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                         PostRepository postRepository,
                         UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new comment on a post
     * Validates that post exists, sets the author, and saves the comment
     * 
     * @param postId the ID of the post to comment on
     * @param request the create comment request containing comment data
     * @param username the username of the authenticated user creating the comment
     * @return CommentDTO representing the created comment
     * @throws ResourceNotFoundException if post is not found
     * @throws ResourceNotFoundException if user is not found
     */
    public CommentDTO createComment(Long postId, CreateCommentRequest request, String username) {
        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // Find the author user
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", Long.valueOf(username.hashCode())));

        // Create comment entity using conversion method
        Comment comment = convertToEntity(request);
        comment.setPost(post);
        comment.setUser(author);

        // Save and return DTO
        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    /**
     * Updates an existing comment
     * Validates that comment exists and belongs to the specified post, updates content, preserves author
     * 
     * @param postId the ID of the post the comment belongs to
     * @param commentId the ID of the comment to update
     * @param request the update comment request containing updated data
     * @return CommentDTO representing the updated comment
     * @throws ResourceNotFoundException if comment is not found or doesn't belong to the specified post
     */
    public CommentDTO updateComment(Long postId, Long commentId, UpdateCommentRequest request) {
        // Find existing comment with author eagerly loaded
        Comment comment = commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Validate comment belongs to the specified post
        if (!comment.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }

        // Update comment content using conversion method (preserves author)
        updateEntityFromDTO(comment, request);

        // Save and return DTO
        Comment updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment);
    }

    /**
     * Retrieves a comment by ID
     * Validates that comment belongs to the specified post
     * 
     * @param postId the ID of the post the comment belongs to
     * @param commentId the ID of the comment to retrieve
     * @return CommentDTO representing the comment
     * @throws ResourceNotFoundException if comment is not found or doesn't belong to the specified post
     */
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long postId, Long commentId) {
        Comment comment = commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Validate comment belongs to the specified post
        if (!comment.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }

        return convertToDTO(comment);
    }

    /**
     * Retrieves all comments for a specific post
     * Validates that post exists
     * 
     * @param postId the ID of the post to retrieve comments for
     * @return List of CommentDTO representing all comments for the post
     * @throws ResourceNotFoundException if post is not found
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        // Validate post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // Get all comments for the post
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a comment by ID
     * Validates that comment exists and belongs to the specified post
     * 
     * @param postId the ID of the post the comment belongs to
     * @param commentId the ID of the comment to delete
     * @throws ResourceNotFoundException if comment is not found or doesn't belong to the specified post
     */
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Validate comment belongs to the specified post
        if (!comment.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }

        commentRepository.delete(comment);
    }

    /**
     * Converts a Comment entity to CommentDTO
     * Maps entity fields to DTO including author username
     * 
     * Validates Requirements: 9.5, 10.4
     * 
     * @param comment the comment entity to convert
     * @return CommentDTO with all fields populated including author username
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorUsername(comment.getUser().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }

    /**
     * Converts CreateCommentRequest DTO to Comment entity
     * Creates a new Comment entity and sets fields from the request DTO
     * Does not set author or post - those must be set separately
     * 
     * Validates Requirements: 10.5
     * 
     * @param request the create comment request DTO
     * @return Comment entity with basic fields populated
     */
    private Comment convertToEntity(CreateCommentRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        return comment;
    }

    /**
     * Updates Comment entity fields from UpdateCommentRequest DTO
     * Updates content field, does not modify author, post, timestamps, or ID
     * 
     * Validates Requirements: 10.6
     * 
     * @param comment the comment entity to update
     * @param request the update comment request DTO containing new values
     */
    private void updateEntityFromDTO(Comment comment, UpdateCommentRequest request) {
        comment.setContent(request.getContent());
    }
}
