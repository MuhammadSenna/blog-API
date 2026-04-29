package com.example.blog.controller;

import com.example.blog.repository.UserRepository;
import com.example.blog.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for AuthController.
 * Tests controller structure and dependency injection.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider
        );
    }

    @Test
    void testControllerCreation_WithAllDependencies_Success() {
        // Verify that the controller can be instantiated with all required dependencies
        assertNotNull(authController);
    }
}
