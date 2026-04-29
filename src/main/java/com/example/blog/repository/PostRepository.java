package com.example.blog.repository;

import com.example.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Post entity
 * Provides custom queries with JOIN FETCH to avoid N+1 problems
 * 
 * Validates Requirements: 10.7
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * Find post by ID with all related entities eagerly loaded
     * Uses JOIN FETCH to load user and category, LEFT JOIN FETCH for tags in a single query
     * Avoids N+1 query problem by fetching all relations in one query
     * 
     * Validates Requirements: 10.7, 10.8
     * 
     * @param id the post ID
     * @return Optional containing the post with related entities, or empty if not found
     */
    @Query("SELECT p FROM Post p " +
           "JOIN FETCH p.user " +
           "JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.id = :id")
    Optional<Post> findByIdWithRelations(@Param("id") Long id);
}
