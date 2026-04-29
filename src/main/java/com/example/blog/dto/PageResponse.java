package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper class for paginated responses
 * Contains the page content and pagination metadata
 * 
 * Validates Requirements: 6.7, 6.8
 * 
 * @param <T> The type of content in the page (e.g., PostDTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * List of items for the current page
     */
    private List<T> content;
    
    /**
     * Current page number (zero-indexed)
     */
    private int pageNumber;
    
    /**
     * Number of items per page
     */
    private int pageSize;
    
    /**
     * Total number of items across all pages
     */
    private long totalElements;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * True if this is the first page
     */
    private boolean isFirst;
    
    /**
     * True if this is the last page
     */
    private boolean isLast;
}
