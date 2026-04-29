package com.example.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT Token Provider for generating and managing JWT tokens.
 * Handles token generation with configurable secret and expiration.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a JWT token for the given username.
     * 
     * @param username the username to include in the token subject claim
     * @return the generated JWT token as a string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates a JWT token by verifying its signature and expiration.
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            
            return true;
        } catch (ExpiredJwtException ex) {
            logger.error("JWT token is expired: {}", ex.getMessage());
        } catch (SignatureException ex) {
            logger.error("JWT signature validation failed: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        
        return false;
    }

    /**
     * Extracts the username from a valid JWT token.
     * 
     * @param token the JWT token to extract username from
     * @return the username contained in the token's subject claim
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String getUsernameFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getSubject();
        } catch (ExpiredJwtException ex) {
            logger.error("Cannot extract username from expired JWT token: {}", ex.getMessage());
            throw ex;
        } catch (SignatureException ex) {
            logger.error("Cannot extract username from JWT token with invalid signature: {}", ex.getMessage());
            throw ex;
        } catch (MalformedJwtException ex) {
            logger.error("Cannot extract username from malformed JWT token: {}", ex.getMessage());
            throw ex;
        } catch (UnsupportedJwtException ex) {
            logger.error("Cannot extract username from unsupported JWT token: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.error("Cannot extract username from JWT token with empty claims: {}", ex.getMessage());
            throw ex;
        }
    }
}
