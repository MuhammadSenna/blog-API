package com.example.blog.exception;

/**
 * Custom exception thrown when a requested resource is not found
 * Used for consistent 404 error handling across the application
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceType;
    private final Long resourceId;
    
    /**
     * Constructs a new ResourceNotFoundException with the specified resource type and ID
     * Automatically generates a message in the format: "{resourceType} not found with id: {resourceId}"
     * 
     * @param resourceType the type of resource that was not found (e.g., "Post", "Comment")
     * @param resourceId the ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(String.format("%s not found with id: %d", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Gets the type of resource that was not found
     * 
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Gets the ID of the resource that was not found
     * 
     * @return the resource ID
     */
    public Long getResourceId() {
        return resourceId;
    }
}
