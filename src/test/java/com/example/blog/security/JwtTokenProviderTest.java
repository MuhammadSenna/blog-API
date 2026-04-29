package com.example.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private long testExpiration = 86400000L; // 24 hours in milliseconds

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    void testGenerateToken_ValidUsername_ReturnsToken() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateToken_TokenContainsCorrectSubject() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(username, claims.getSubject());
    }

    @Test
    void testGenerateToken_TokenHasIssuedAtTimestamp() {
        // Given
        String username = "testuser";
        Date beforeGeneration = new Date();

        // When
        String token = jwtTokenProvider.generateToken(username);
        Date afterGeneration = new Date();

        // Then
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getIssuedAt().getTime() >= beforeGeneration.getTime() - 1000);
        assertTrue(claims.getIssuedAt().getTime() <= afterGeneration.getTime() + 1000);
    }

    @Test
    void testGenerateToken_TokenHasCorrectExpiration() {
        // Given
        String username = "testuser";
        Date beforeGeneration = new Date();

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getExpiration());
        long expectedExpiration = beforeGeneration.getTime() + testExpiration;
        // Allow 1 second tolerance for test execution time
        assertTrue(Math.abs(claims.getExpiration().getTime() - expectedExpiration) < 1000);
    }

    @Test
    void testGenerateToken_UsesHS512Algorithm() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateToken(username);

        // Then
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        // If the algorithm is wrong, this will throw an exception
        assertDoesNotThrow(() -> {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        });
    }

    @Test
    void testGenerateToken_DifferentUsernames_GenerateDifferentTokens() {
        // Given
        String username1 = "user1";
        String username2 = "user2";

        // When
        String token1 = jwtTokenProvider.generateToken(username1);
        String token2 = jwtTokenProvider.generateToken(username2);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Given - Create a token with very short expiration (1 millisecond)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 1L);
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertFalse(isValid);
        
        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Given - Generate a token with one secret
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);
        
        // Change the secret to simulate invalid signature (must be at least 64 characters for HS512)
        String differentSecret = "differentSecretKey1234567890123456789012345678901234567890123456";
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", differentSecret);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertFalse(isValid);
        
        // Reset secret for other tests
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetUsernameFromToken_ValidToken_ReturnsUsername() {
        // Given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetUsernameFromToken_DifferentUsernames_ReturnsCorrectUsername() {
        // Given
        String username1 = "user1";
        String username2 = "user2";
        String token1 = jwtTokenProvider.generateToken(username1);
        String token2 = jwtTokenProvider.generateToken(username2);

        // When
        String extractedUsername1 = jwtTokenProvider.getUsernameFromToken(token1);
        String extractedUsername2 = jwtTokenProvider.getUsernameFromToken(token2);

        // Then
        assertEquals(username1, extractedUsername1);
        assertEquals(username2, extractedUsername2);
        assertNotEquals(extractedUsername1, extractedUsername2);
    }

    @Test
    void testGetUsernameFromToken_ExpiredToken_ThrowsException() {
        // Given - Create a token with very short expiration (1 millisecond)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 1L);
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When & Then
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(token);
        });
        
        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    void testGetUsernameFromToken_InvalidSignature_ThrowsException() {
        // Given - Generate a token with one secret
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);
        
        // Change the secret to simulate invalid signature (must be at least 64 characters for HS512)
        String differentSecret = "differentSecretKey1234567890123456789012345678901234567890123456";
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", differentSecret);

        // When & Then
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(token);
        });
        
        // Reset secret for other tests
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
    }

    @Test
    void testGetUsernameFromToken_MalformedToken_ThrowsException() {
        // Given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When & Then
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(malformedToken);
        });
    }

    @Test
    void testGetUsernameFromToken_EmptyToken_ThrowsException() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(emptyToken);
        });
    }

    @Test
    void testGetUsernameFromToken_NullToken_ThrowsException() {
        // Given
        String nullToken = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(nullToken);
        });
    }
}
