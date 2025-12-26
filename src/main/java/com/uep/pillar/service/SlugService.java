package com.uep.pillar.service;

/**
 * Slug generation and sanitization utilities.
 * Ensures unique, URL-safe slugs across supported entities.
 */
public interface SlugService {

    /**
     * Generate a unique slug from a title.
     * Rules:
     * - Convert to lowercase
     * - Replace spaces with hyphens
     * - Remove special characters
     * - Limit to 100 characters
     * - Append number if duplicate (e.g., "my-article-2")
     *
     * @param title       Source title
     * @param entityClass Entity class to check uniqueness against (Article, Category, Tag, PublicationIssue)
     * @param excludeId   Optional entity ID to exclude from uniqueness check (for updates)
     * @return Unique slug string
     */
    String generateUniqueSlug(String title, Class<?> entityClass, Long excludeId);

    /**
     * Sanitize a string into slug format.
     * @param input Raw input
     * @return Sanitized slug base
     */
    String sanitize(String input);
}
