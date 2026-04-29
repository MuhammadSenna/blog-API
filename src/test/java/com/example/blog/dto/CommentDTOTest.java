package com.example.blog.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommentDTO
 * Tests response structure for Requirements 1.1, 1.2
 */
class CommentDTOTest {
    
    @Test
    void testCommentDTOWithAllFields() {
        Long id = 1L;
        String content = "This is a test comment";
        String authorUsername = "testuser";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        CommentDTO commentDTO = new CommentDTO(id, content, authorUsername, createdAt, updatedAt);
        
        assertEquals(id, commentDTO.getId());
        assertEquals(content, commentDTO.getContent());
        assertEquals(authorUsername, commentDTO.getAuthorUsername());
        assertEquals(createdAt, commentDTO.getCreatedAt());
        assertEquals(updatedAt, commentDTO.getUpdatedAt());
    }
    
    @Test
    void testCommentDTONoArgsConstructor() {
        CommentDTO commentDTO = new CommentDTO();
        
        assertNotNull(commentDTO);
        assertNull(commentDTO.getId());
        assertNull(commentDTO.getContent());
        assertNull(commentDTO.getAuthorUsername());
    }
    
    @Test
    void testCommentDTOSetters() {
        CommentDTO commentDTO = new CommentDTO();
        
        commentDTO.setId(1L);
        commentDTO.setContent("New Comment");
        commentDTO.setAuthorUsername("author");
        LocalDateTime now = LocalDateTime.now();
        commentDTO.setCreatedAt(now);
        commentDTO.setUpdatedAt(now);
        
        assertEquals(1L, commentDTO.getId());
        assertEquals("New Comment", commentDTO.getContent());
        assertEquals("author", commentDTO.getAuthorUsername());
        assertEquals(now, commentDTO.getCreatedAt());
        assertEquals(now, commentDTO.getUpdatedAt());
    }
    
    @Test
    void testCommentDTOEquality() {
        LocalDateTime now = LocalDateTime.now();
        
        CommentDTO commentDTO1 = new CommentDTO(1L, "Content", "user", now, now);
        CommentDTO commentDTO2 = new CommentDTO(1L, "Content", "user", now, now);
        
        assertEquals(commentDTO1, commentDTO2, "CommentDTOs with same values should be equal");
    }
    
    @Test
    void testCommentDTOToString() {
        CommentDTO commentDTO = new CommentDTO(1L, "Test Content", "testuser", 
                                               LocalDateTime.now(), LocalDateTime.now());
        
        String toString = commentDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Test Content"));
        assertTrue(toString.contains("testuser"));
    }
}
