package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for creating a new blog post
 * Validates: Requirements 1.4, 2.2, 2.3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private Set<Long> tagIds;
}
