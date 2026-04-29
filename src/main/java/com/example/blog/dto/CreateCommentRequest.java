package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new comment on a blog post
 * Validates: Requirements 1.6, 2.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
}
