package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Category;
import com.uep.pillar.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for Category-related queries.
 */
@Component
@RequiredArgsConstructor
public class CategoryQueryResolver {

    private final CategoryService categoryService;

    /**
     * Get all categories.
     *
     * @return list of all categories
     */
    public List<Category> categories() {
        return categoryService.findAll();
    }

    /**
     * Get a category by its slug.
     *
     * @param slug the category slug (e.g., "news", "opinion")
     * @return the category if found, null otherwise
     */
    public Category categoryBySlug(String slug) {
        return categoryService.findBySlug(slug).orElse(null);
    }

    /**
     * Get a category by ID.
     *
     * @param id the category ID
     * @return the category if found, null otherwise
     */
    public Category category(String id) {
        try {
            Integer categoryId = Integer.parseInt(id);
            return categoryService.findById(categoryId);
        } catch (NumberFormatException | com.uep.pillar.exception.ResourceNotFoundException e) {
            return null;
        }
    }
}

