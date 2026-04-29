package com.example.blog.repository;

import com.example.blog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository custom query methods.
 * 
 * Tests validate Requirements 10.1, 10.2, 10.3, 10.4, 10.5:
 * - Find user by username
 * - Find user by email
 * - Check if username exists
 * - Check if email exists
 * - Return empty Optional or false when not found
 */
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        userRepository.deleteAll();
        
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        
        // Persist the test user
        userRepository.save(testUser);
    }

    @Test
    void testFindByUsername_ExistingUsername_ReturnsUser() {
        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent(), "User should be found by username");
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsername_NonExistingUsername_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(found.isPresent(), "Should return empty Optional for non-existing username");
    }

    @Test
    void testFindByUsername_NullUsername_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername(null);

        // Then
        assertFalse(found.isPresent(), "Should return empty Optional for null username");
    }

    @Test
    void testFindByEmail_ExistingEmail_ReturnsUser() {
        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent(), "User should be found by email");
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent(), "Should return empty Optional for non-existing email");
    }

    @Test
    void testFindByEmail_NullEmail_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail(null);

        // Then
        assertFalse(found.isPresent(), "Should return empty Optional for null email");
    }

    @Test
    void testExistsByUsername_ExistingUsername_ReturnsTrue() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertTrue(exists, "Should return true for existing username");
    }

    @Test
    void testExistsByUsername_NonExistingUsername_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertFalse(exists, "Should return false for non-existing username");
    }

    @Test
    void testExistsByUsername_NullUsername_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByUsername(null);

        // Then
        assertFalse(exists, "Should return false for null username");
    }

    @Test
    void testExistsByEmail_ExistingEmail_ReturnsTrue() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists, "Should return true for existing email");
    }

    @Test
    void testExistsByEmail_NonExistingEmail_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists, "Should return false for non-existing email");
    }

    @Test
    void testExistsByEmail_NullEmail_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail(null);

        // Then
        assertFalse(exists, "Should return false for null email");
    }

    @Test
    void testMultipleUsers_FindByUsername_ReturnsCorrectUser() {
        // Given: Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("hashedPassword456");
        userRepository.save(anotherUser);

        // When
        Optional<User> found = userRepository.findByUsername("anotheruser");

        // Then
        assertTrue(found.isPresent(), "Should find the second user");
        assertEquals("anotheruser", found.get().getUsername());
        assertEquals("another@example.com", found.get().getEmail());
    }

    @Test
    void testMultipleUsers_FindByEmail_ReturnsCorrectUser() {
        // Given: Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("hashedPassword456");
        userRepository.save(anotherUser);

        // When
        Optional<User> found = userRepository.findByEmail("another@example.com");

        // Then
        assertTrue(found.isPresent(), "Should find the second user");
        assertEquals("anotheruser", found.get().getUsername());
        assertEquals("another@example.com", found.get().getEmail());
    }
}
