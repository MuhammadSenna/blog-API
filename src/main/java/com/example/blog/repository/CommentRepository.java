package com.example.blog.repository;

import com.example.blog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Find all comments for a specific post with author eagerly loaded
     * Uses JOIN FETCH to avoid N+1 query problem
     * 
     * @param postId the ID of the post
     * @return List of comments for the post with authors loaded
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
    List<Comment> findByPostId(@Param("postId") Long postId);
    
    /**
     * Find a comment by ID with author eagerly loaded
     * Uses JOIN FETCH to avoid N+1 query problem
     * 
     * @param id the ID of the comment
     * @return Optional containing the comment with author loaded, or empty if not found
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Comment> findByIdWithAuthor(@Param("id") Long id);
    
    /**
     * Find a comment by ID with user eagerly loaded
     * Uses JOIN FETCH to avoid N+1 query problem
     * 
     * @param id the ID of the comment
     * @return Optional containing the comment with user loaded, or empty if not found
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Comment> findByIdWithUser(@Param("id") Long id);
    
    /**
     * Find all comments for a specific post with user eagerly loaded
     * Uses JOIN FETCH to avoid N+1 query problem
     * 
     * @param postId the ID of the post
     * @return List of comments for the post with users loaded
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);
}
