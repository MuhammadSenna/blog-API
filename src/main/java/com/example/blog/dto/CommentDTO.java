package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for comments
 * Contains comment information including author username
 * 
 * Validates Requirements: 1.1, 1.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    
    private Long id;
    
    private String content;
    
    private String authorUsername;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
