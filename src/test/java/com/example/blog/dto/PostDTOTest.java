package com.example.blog.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostDTO
 * Tests response structure for Requirements 1.1, 1.2
 */
class PostDTOTest {
    
    @Test
    void testPostDTOWithAllFields() {
        Long id = 1L;
        String title = "Test Post";
        String content = "This is test content";
        String authorUsername = "testuser";
        String categoryName = "Technology";
        Set<String> tagNames = Set.of("java", "spring", "testing");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        PostDTO postDTO = new PostDTO(id, title, content, authorUsername, categoryName, 
                                      tagNames, createdAt, updatedAt);
        
        assertEquals(id, postDTO.getId());
        assertEquals(title, postDTO.getTitle());
        assertEquals(content, postDTO.getContent());
        assertEquals(authorUsername, postDTO.getAuthorUsername());
        assertEquals(categoryName, postDTO.getCategoryName());
        assertEquals(tagNames, postDTO.getTagNames());
        assertEquals(createdAt, postDTO.getCreatedAt());
        assertEquals(updatedAt, postDTO.getUpdatedAt());
    }
    
    @Test
    void testPostDTONoArgsConstructor() {
        PostDTO postDTO = new PostDTO();
        
        assertNotNull(postDTO);
        assertNull(postDTO.getId());
        assertNull(postDTO.getTitle());
        assertNull(postDTO.getContent());
    }
    
    @Test
    void testPostDTOSetters() {
        PostDTO postDTO = new PostDTO();
        
        postDTO.setId(1L);
        postDTO.setTitle("New Title");
        postDTO.setContent("New Content");
        postDTO.setAuthorUsername("author");
        postDTO.setCategoryName("Category");
        postDTO.setTagNames(Set.of("tag1", "tag2"));
        LocalDateTime now = LocalDateTime.now();
        postDTO.setCreatedAt(now);
        postDTO.setUpdatedAt(now);
        
        assertEquals(1L, postDTO.getId());
        assertEquals("New Title", postDTO.getTitle());
        assertEquals("New Content", postDTO.getContent());
        assertEquals("author", postDTO.getAuthorUsername());
        assertEquals("Category", postDTO.getCategoryName());
        assertEquals(Set.of("tag1", "tag2"), postDTO.getTagNames());
        assertEquals(now, postDTO.getCreatedAt());
        assertEquals(now, postDTO.getUpdatedAt());
    }
    
    @Test
    void testPostDTOEquality() {
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags = Set.of("java", "spring");
        
        PostDTO postDTO1 = new PostDTO(1L, "Title", "Content", "user", "Category", 
                                       tags, now, now);
        PostDTO postDTO2 = new PostDTO(1L, "Title", "Content", "user", "Category", 
                                       tags, now, now);
        
        assertEquals(postDTO1, postDTO2, "PostDTOs with same values should be equal");
    }
    
    @Test
    void testPostDTOToString() {
        PostDTO postDTO = new PostDTO(1L, "Title", "Content", "user", "Category", 
                                      Set.of("tag1"), LocalDateTime.now(), LocalDateTime.now());
        
        String toString = postDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Title"));
        assertTrue(toString.contains("user"));
        assertTrue(toString.contains("Category"));
    }
    
    @Test
    void testPostDTOWithEmptyTagNames() {
        PostDTO postDTO = new PostDTO();
        postDTO.setTagNames(Set.of());
        
        assertNotNull(postDTO.getTagNames());
        assertTrue(postDTO.getTagNames().isEmpty());
    }
}
