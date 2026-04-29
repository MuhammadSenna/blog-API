package com.example.blog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for JWT-based authentication.
 * 
 * Validates Requirements:
 * - 5.1: POST /api/posts endpoint protected with @PreAuthorize("isAuthenticated()")
 * - 5.2: PUT /api/posts/{id} endpoint protected with @PreAuthorize("isAuthenticated()")
 * - 5.3: DELETE /api/posts/{id} endpoint protected with @PreAuthorize("isAuthenticated()")
 * - 5.4: POST /api/posts/{postId}/comments endpoint protected with @PreAuthorize("isAuthenticated()")
 * - 5.5: PUT /api/posts/{postId}/comments/{commentId} endpoint protected with @PreAuthorize("isAuthenticated()")
 * - 5.6: DELETE /api/posts/{postId}/comments/{commentId} endpoint protected with @PreAuthorize("isAuthenticated()")
 * - 7.1: Permit all requests to /api/auth/** without authentication
 * - 7.2: Require authentication for all other API endpoints
 * - 7.3: Disable CSRF protection for stateless JWT authentication
 * - 7.4: Configure session management to be stateless
 * - 7.5: Register the Authentication_Filter in the filter chain
 * - 7.6: Configure AuthenticationManager bean for authentication processing
 * - 7.7: Configure password encoding using BCryptPasswordEncoder
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor injection of JwtAuthenticationFilter.
     * 
     * @param jwtAuthenticationFilter the JWT authentication filter
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures HTTP security with JWT authentication.
     * Sets up stateless session management, disables CSRF, and configures authorization rules.
     * 
     * @param http HttpSecurity to configure
     * @return SecurityFilterChain configured for JWT authentication
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure session management to STATELESS
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Permit all requests to /api/auth/** (registration and login)
                .requestMatchers("/api/auth/**").permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            
            // Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Provides BCrypt password encoder for secure password hashing.
     * Uses default strength of 10 rounds.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides authentication manager for processing authentication requests.
     * Configured with CustomUserDetailsService and PasswordEncoder.
     * 
     * @param config Spring Security's authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
