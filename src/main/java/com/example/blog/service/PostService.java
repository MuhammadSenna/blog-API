package com.example.blog.service;

import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.PageResponse;
import com.example.blog.dto.PostDTO;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing blog posts
 * Handles business logic for CRUD operations on posts
 * 
 * Validates Requirements: 9.1, 9.2, 9.6, 9.8
 */
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository postRepository, 
                      UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Creates a new post
     * Validates that category and tags exist, sets the author, and saves the post
     * 
     * @param request the create post request containing post data
     * @param username the username of the authenticated user creating the post
     * @return PostDTO representing the created post
     * @throws ResourceNotFoundException if category or any tag is not found
     * @throws ResourceNotFoundException if user is not found
     */
    public PostDTO createPost(CreatePostRequest request, String username) {
        // Find the author user
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", Long.valueOf(username.hashCode())));

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // Validate tags exist
        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            if (tags.size() != request.getTagIds().size()) {
                throw new ResourceNotFoundException("Tag", 0L);
            }
        }

        // Create post entity using conversion method
        Post post = convertToEntity(request);
        post.setUser(author);
        post.setCategory(category);
        post.setTags(tags);

        // Save and return DTO
        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    /**
     * Updates an existing post
     * Validates that post, category, and tags exist, updates fields, preserves author
     * 
     * @param id the ID of the post to update
     * @param request the update post request containing updated data
     * @return PostDTO representing the updated post
     * @throws ResourceNotFoundException if post, category, or any tag is not found
     */
    public PostDTO updatePost(Long id, UpdatePostRequest request) {
        // Find existing post
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // Validate tags exist
        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            if (tags.size() != request.getTagIds().size()) {
                throw new ResourceNotFoundException("Tag", 0L);
            }
        }

        // Update post fields using conversion method (preserves author)
        updateEntityFromDTO(post, request);
        post.setCategory(category);
        post.setTags(tags);

        // Save and return DTO
        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    /**
     * Retrieves a post by ID
     * Uses JOIN FETCH query to avoid N+1 problems when loading related entities
     * 
     * @param id the ID of the post to retrieve
     * @return PostDTO representing the post
     * @throws ResourceNotFoundException if post is not found
     */
    @Transactional(readOnly = true)
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        return convertToDTO(post);
    }

    /**
     * Retrieves all posts with pagination
     * 
     * @param pageable pagination information (page number, size, sort)
     * @return PageResponse containing the list of posts and pagination metadata
     */
    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);
        
        List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                postDTOs,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isFirst(),
                postPage.isLast()
        );
    }

    /**
     * Deletes a post by ID
     * 
     * @param id the ID of the post to delete
     * @throws ResourceNotFoundException if post is not found
     */
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        postRepository.delete(post);
    }

    /**
     * Converts a Post entity to PostDTO
     * Maps entity fields to DTO including related entity names
     * 
     * Validates Requirements: 9.3, 10.1, 10.7, 10.8
     * 
     * @param post the post entity to convert
     * @return PostDTO with all fields populated including author username, category name, and tag names
     */
    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorUsername(post.getUser().getUsername());
        dto.setCategoryName(post.getCategory() != null ? post.getCategory().getName() : null);
        dto.setTagNames(post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }

    /**
     * Converts CreatePostRequest DTO to Post entity
     * Creates a new Post entity and sets fields from the request DTO
     * Does not set author, category, or tags - those must be set separately
     * 
     * Validates Requirements: 10.2
     * 
     * @param request the create post request DTO
     * @return Post entity with basic fields populated
     */
    private Post convertToEntity(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        return post;
    }

    /**
     * Updates Post entity fields from UpdatePostRequest DTO
     * Updates title and content fields, does not modify author, timestamps, or ID
     * Category and tags must be set separately after validation
     * 
     * Validates Requirements: 10.3
     * 
     * @param post the post entity to update
     * @param request the update post request DTO containing new values
     */
    private void updateEntityFromDTO(Post post, UpdatePostRequest request) {
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
    }
}
