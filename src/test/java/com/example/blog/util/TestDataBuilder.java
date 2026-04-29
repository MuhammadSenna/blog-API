package com.example.blog.util;

import com.example.blog.entity.Category;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for creating test data in integration tests.
 * Provides static factory methods for creating test entities.
 *
 * Validates Requirements: 8.1
 */
public class TestDataBuilder {

    public static User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$10$encodedPasswordHash");
        return user;
    }

    public static Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test category: " + name);
        return category;
    }

    public static Tag createTestTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tag;
    }

    public static Post createTestPost(User user, Category category, Set<Tag> tags) {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setContent("Test Content for integration testing");
        post.setUser(user);
        post.setCategory(category);
        post.setTags(tags != null ? tags : new HashSet<>());
        return post;
    }

    public static Post createTestPost(String title, String content, User user, Category category) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setUser(user);
        post.setCategory(category);
        post.setTags(new HashSet<>());
        return post;
    }

    public static Comment createTestComment(User user, Post post) {
        Comment comment = new Comment();
        comment.setContent("Test Comment content");
        comment.setUser(user);
        comment.setPost(post);
        return comment;
    }

    public static Comment createTestComment(String content, User user, Post post) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        return comment;
    }
}
