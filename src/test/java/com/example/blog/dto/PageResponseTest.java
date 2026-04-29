package com.example.blog.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PageResponse
 * Tests pagination wrapper structure for Requirements 6.7, 6.8
 */
class PageResponseTest {
    
    @Test
    void testPageResponseWithAllFields() {
        List<String> content = List.of("item1", "item2", "item3");
        int pageNumber = 0;
        int pageSize = 10;
        long totalElements = 25L;
        int totalPages = 3;
        boolean isFirst = true;
        boolean isLast = false;
        
        PageResponse<String> pageResponse = new PageResponse<>(content, pageNumber, pageSize, 
                                                               totalElements, totalPages, isFirst, isLast);
        
        assertEquals(content, pageResponse.getContent());
        assertEquals(pageNumber, pageResponse.getPageNumber());
        assertEquals(pageSize, pageResponse.getPageSize());
        assertEquals(totalElements, pageResponse.getTotalElements());
        assertEquals(totalPages, pageResponse.getTotalPages());
        assertTrue(pageResponse.isFirst());
        assertFalse(pageResponse.isLast());
    }
    
    @Test
    void testPageResponseNoArgsConstructor() {
        PageResponse<String> pageResponse = new PageResponse<>();
        
        assertNotNull(pageResponse);
        assertNull(pageResponse.getContent());
        assertEquals(0, pageResponse.getPageNumber());
        assertEquals(0, pageResponse.getPageSize());
    }
    
    @Test
    void testPageResponseSetters() {
        PageResponse<String> pageResponse = new PageResponse<>();
        
        List<String> content = List.of("test");
        pageResponse.setContent(content);
        pageResponse.setPageNumber(1);
        pageResponse.setPageSize(20);
        pageResponse.setTotalElements(100L);
        pageResponse.setTotalPages(5);
        pageResponse.setFirst(false);
        pageResponse.setLast(false);
        
        assertEquals(content, pageResponse.getContent());
        assertEquals(1, pageResponse.getPageNumber());
        assertEquals(20, pageResponse.getPageSize());
        assertEquals(100L, pageResponse.getTotalElements());
        assertEquals(5, pageResponse.getTotalPages());
        assertFalse(pageResponse.isFirst());
        assertFalse(pageResponse.isLast());
    }
    
    @Test
    void testPageResponseWithEmptyContent() {
        PageResponse<String> pageResponse = new PageResponse<>(List.of(), 0, 10, 0L, 0, true, true);
        
        assertNotNull(pageResponse.getContent());
        assertTrue(pageResponse.getContent().isEmpty());
        assertTrue(pageResponse.isFirst());
        assertTrue(pageResponse.isLast());
    }
    
    @Test
    void testPageResponseWithDifferentType() {
        List<Integer> content = List.of(1, 2, 3);
        PageResponse<Integer> pageResponse = new PageResponse<>(content, 0, 10, 3L, 1, true, true);
        
        assertEquals(content, pageResponse.getContent());
        assertEquals(Integer.class, pageResponse.getContent().get(0).getClass());
    }
    
    @Test
    void testPageResponseEquality() {
        List<String> content = List.of("item1", "item2");
        
        PageResponse<String> pageResponse1 = new PageResponse<>(content, 0, 10, 2L, 1, true, true);
        PageResponse<String> pageResponse2 = new PageResponse<>(content, 0, 10, 2L, 1, true, true);
        
        assertEquals(pageResponse1, pageResponse2, "PageResponses with same values should be equal");
    }
    
    @Test
    void testPageResponseToString() {
        PageResponse<String> pageResponse = new PageResponse<>(List.of("item"), 0, 10, 1L, 1, true, true);
        
        String toString = pageResponse.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("item"));
    }
    
    @Test
    void testPageResponseLastPage() {
        PageResponse<String> pageResponse = new PageResponse<>(List.of("item"), 2, 10, 25L, 3, false, true);
        
        assertFalse(pageResponse.isFirst());
        assertTrue(pageResponse.isLast());
        assertEquals(2, pageResponse.getPageNumber());
        assertEquals(3, pageResponse.getTotalPages());
    }
}
