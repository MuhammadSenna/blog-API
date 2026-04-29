package com.example.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * Tests token extraction, validation, and authentication setup logic.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
        // Clear SecurityContext before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidBearerToken_SetsAuthentication() throws ServletException, IOException {
        // Given: Request with valid Bearer token
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.test";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User("testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is set in SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(customUserDetailsService).loadUserByUsername("testuser");
    }

    @Test
    void testDoFilterInternal_WithInvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given: Request with invalid Bearer token
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "invalid.token.here";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set in SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void testDoFilterInternal_WithoutAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given: Request without Authorization header
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithInvalidAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given: Request with Authorization header without Bearer prefix
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithEmptyBearerToken_ContinuesFilterChain() throws ServletException, IOException {
        // Given: Request with Bearer prefix but no token
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer ");

        when(jwtTokenProvider.validateToken("")).thenReturn(false);

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithEmptyAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given: Request with empty Authorization header
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "");

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithBearerTokenLowerCase_ContinuesFilterChain() throws ServletException, IOException {
        // Given: Request with lowercase "bearer" prefix (should not be extracted)
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "bearer eyJhbGciOiJIUzUxMiJ9.test");

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set (token not extracted due to case sensitivity)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WhenExceptionOccurs_ContinuesFilterChain() throws ServletException, IOException {
        // Given: Request with valid Bearer token but exception during processing
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.test";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenThrow(new RuntimeException("Token parsing error"));

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication is not set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        // And: Filter chain continues despite exception
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithValidToken_LoadsUserDetailsCorrectly() throws ServletException, IOException {
        // Given: Request with valid Bearer token
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User("john.doe", "encodedPassword", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("john.doe");
        when(customUserDetailsService.loadUserByUsername("john.doe")).thenReturn(userDetails);

        // When: Filter processes request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Authentication contains correct user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().containsAll(userDetails.getAuthorities()));

        // And: Filter chain continues
        verify(filterChain).doFilter(request, response);
    }
}
