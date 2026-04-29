package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for blog posts
 * Contains all post information including related entity names
 * 
 * Validates Requirements: 1.1, 1.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private String authorUsername;
    
    private String categoryName;
    
    private Set<String> tagNames;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
