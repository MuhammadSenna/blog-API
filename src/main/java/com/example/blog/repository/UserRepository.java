package com.example.blog.repository;

import com.example.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity with custom query methods.
 * 
 * Validates Requirements:
 * - 10.1: Find user by username
 * - 10.2: Find user by email
 * - 10.3: Check if username exists
 * - 10.4: Check if email exists
 * - 10.5: Return empty Optional or false when not found
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by email.
     * 
     * @param email the email to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a username exists in the database.
     * 
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Checks if an email exists in the database.
     * 
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
