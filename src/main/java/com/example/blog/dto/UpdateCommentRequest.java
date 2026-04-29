package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing comment
 * Validates: Requirements 1.7, 2.7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
}
