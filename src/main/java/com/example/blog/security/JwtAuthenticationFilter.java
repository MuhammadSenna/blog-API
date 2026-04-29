package com.example.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter for validating JWT tokens in HTTP requests.
 * Extends OncePerRequestFilter to ensure single execution per request.
 * 
 * Validates Requirements:
 * - 6.1: Extend Spring Security OncePerRequestFilter
 * - 6.2: Extract Authorization header from request
 * - 6.3: Check for "Bearer " prefix and extract JWT token
 * - 6.4: Validate token using JwtTokenProvider
 * - 6.5: Extract username from valid token
 * - 6.6: Load user details using CustomUserDetailsService
 * - 6.7: Create UsernamePasswordAuthenticationToken with user details and authorities
 * - 6.8: Set authentication in SecurityContextHolder
 * - 6.9: Continue filter chain regardless of token validity
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructor injection of dependencies.
     * 
     * @param jwtTokenProvider the JWT token provider for token validation
     * @param customUserDetailsService the user details service for loading user information
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                   CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Filters each HTTP request to extract and validate JWT tokens.
     * Extracts the Authorization header, checks for Bearer prefix, extracts the JWT token,
     * validates it, loads user details, and sets authentication in the SecurityContext.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract JWT token from Authorization header
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null) {
                logger.debug("JWT token extracted from request: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
                
                // Validate token using JwtTokenProvider
                if (jwtTokenProvider.validateToken(jwt)) {
                    // Extract username from valid token
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    
                    // Load user details using CustomUserDetailsService
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    
                    // Create UsernamePasswordAuthenticationToken with user details and authorities
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities());
                    
                    // Set authentication in SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("Authentication set in SecurityContext for user: {}", username);
                } else {
                    logger.debug("JWT token validation failed");
                }
            } else {
                logger.debug("No JWT token found in request");
            }
        } catch (Exception ex) {
            // Log errors but don't block request flow
            logger.error("Could not set user authentication in security context: {}", ex.getMessage());
        }
        
        // Continue filter chain regardless of token validity
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     * Checks for "Bearer " prefix and returns the token if present.
     * 
     * @param request the HTTP request
     * @return the JWT token string, or null if not present or invalid format
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            // Extract token after "Bearer " prefix
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}
