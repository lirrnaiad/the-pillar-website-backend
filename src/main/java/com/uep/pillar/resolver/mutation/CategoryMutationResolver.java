package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateCategoryInput;
import com.uep.pillar.dto.UpdateCategoryInput;
import com.uep.pillar.model.Category;
import com.uep.pillar.service.CategoryService;
import com.uep.pillar.service.UserService;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for Category mutations.
 * Handles category creation, updates, and deletion.
 */
@Component
public class CategoryMutationResolver extends BaseMutationResolver {

    private final CategoryService categoryService;

    // UserService is required by BaseMutationResolver for getCurrentUser()
    public CategoryMutationResolver(CategoryService categoryService, UserService userService) {
        super(userService);
        this.categoryService = categoryService;
    }

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
}
