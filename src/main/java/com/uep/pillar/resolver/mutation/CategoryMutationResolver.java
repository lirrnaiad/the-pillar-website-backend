package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateCategoryInput;
import com.uep.pillar.dto.UpdateCategoryInput;
import com.uep.pillar.model.Category;
import com.uep.pillar.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for Category mutations.
 * Handles category creation, updates, and deletion.
 */
@Component
@RequiredArgsConstructor
public class CategoryMutationResolver {

    private final CategoryService categoryService;

    /**
     * Create a new category.
     *
     * @param input category creation input
     * @return the created category
     */
    public Category createCategory(CreateCategoryInput input) {
        // Note: Slug from input is ignored; service auto-generates from name
        return categoryService.create(
            input.getName(),
            input.getDescription(),
            input.getColor()
        );
    }

    /**
     * Update an existing category.
     *
     * @param input category update input
     * @return the updated category
     */
    public Category updateCategory(UpdateCategoryInput input) {
        Integer id = parseIntegerId(input.getId(), "Category ID");
        
        // Note: Slug from input is ignored; service auto-updates if name changes
        return categoryService.update(
            id,
            input.getName(),
            input.getDescription(),
            input.getColor()
        );
    }

    /**
     * Delete a category.
     *
     * @param id the category ID
     * @return true on success
     */
    public Boolean deleteCategory(String id) {
        Integer categoryId = parseIntegerId(id, "Category ID");
        categoryService.delete(categoryId);
        return true;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Parse Integer ID from GraphQL ID string.
     */
    private Integer parseIntegerId(String id, String fieldName) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}
