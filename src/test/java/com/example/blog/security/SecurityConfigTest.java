package com.example.blog.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityConfig.
 * 
 * Tests verify that:
 * - PasswordEncoder bean is properly configured
 * - AuthenticationManager bean is properly configured
 * - BCrypt password encoding works correctly
 * - HTTP security configuration is correct
 * - SecurityFilterChain bean is properly configured
 */
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void testPasswordEncoderBean_IsConfigured() {
        assertNotNull(passwordEncoder, "PasswordEncoder bean should be configured");
    }

    @Test
    void testAuthenticationManagerBean_IsConfigured() {
        assertNotNull(authenticationManager, "AuthenticationManager bean should be configured");
    }

    @Test
    void testSecurityFilterChainBean_IsConfigured() {
        assertNotNull(securityFilterChain, "SecurityFilterChain bean should be configured");
    }

    @Test
    void testPasswordEncoder_EncodePassword_ReturnsEncodedPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"), 
                "Encoded password should start with BCrypt prefix");
    }

    @Test
    void testPasswordEncoder_MatchesPassword_ReturnsTrueForCorrectPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "Password encoder should match correct password");
    }

    @Test
    void testPasswordEncoder_MatchesPassword_ReturnsFalseForIncorrectPassword() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword),
                "Password encoder should not match incorrect password");
    }

    @Test
    void testPasswordEncoder_EncodeSamePasswordTwice_ProducesDifferentHashes() {
        String rawPassword = "testPassword123";
        String encodedPassword1 = passwordEncoder.encode(rawPassword);
        String encodedPassword2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encodedPassword1, encodedPassword2,
                "BCrypt should produce different hashes for same password due to random salt");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1),
                "First hash should match original password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2),
                "Second hash should match original password");
    }
}
