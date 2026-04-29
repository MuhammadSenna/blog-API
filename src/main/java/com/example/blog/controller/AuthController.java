package com.example.blog.controller;

import com.example.blog.dto.AuthResponse;
import com.example.blog.dto.ErrorResponse;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Handles user registration and login.
 * 
 * Validates: Requirements 8.1
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructor injection of dependencies.
     * 
     * @param userRepository the user repository for database access
     * @param passwordEncoder the password encoder for hashing passwords
     * @param authenticationManager the authentication manager for credential validation
     * @param jwtTokenProvider the JWT token provider for token generation
     */
    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user account.
     * 
     * @param request the registration request containing username, email, and password
     * @return ResponseEntity with registration result
     * 
     * Validates: Requirements 8.5, 8.6, 8.7, 8.8, 8.9, 8.10, 12.2, 12.3
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            ErrorResponse errorResponse = new ErrorResponse(
                "Username already exists",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            ErrorResponse errorResponse = new ErrorResponse(
                "Email already exists",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        // Create new user with encoded password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Save user to database
        userRepository.save(user);
        
        // Return success response
        AuthResponse response = new AuthResponse(
            null,  // No token generated during registration
            user.getUsername(),
            user.getEmail()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and generates a JWT token.
     * 
     * @param request the login request containing username/email and password
     * @return ResponseEntity with authentication result
     * 
     * Validates: Requirements 9.1, 9.4, 9.5, 9.6, 9.7, 9.8, 12.1
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Create authentication token with credentials
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(),
                    request.getPassword()
                );
            
            // Authenticate using AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            
            // Generate JWT token using the authenticated username
            String token = jwtTokenProvider.generateToken(authentication.getName());
            
            // Load user details to get username and email
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseGet(() -> userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new UsernameNotFoundException(
                                    "User not found with username or email: " + authentication.getName())));
            
            // Create AuthResponse with token, username, and email
            AuthResponse response = new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail()
            );
            
            // Return 200 OK with AuthResponse
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Handle authentication failure and return 401 Unauthorized with error message
            ErrorResponse errorResponse = new ErrorResponse(
                "Invalid username or password",
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
