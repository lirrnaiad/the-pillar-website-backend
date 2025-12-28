package com.uep.pillar.resolver.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import com.uep.pillar.model.Category;
import com.uep.pillar.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for Category operations.
 * Handles all category-related queries defined in the GraphQL schema.
 */
@Component
@RequiredArgsConstructor
public class CategoryQueryResolver implements GraphQLQueryResolver {

    private final CategoryRepository categoryRepository;

    /**
     * Resolves the 'categories' query.
     * Returns all categories in the system.
     *
     * @return list of all categories
     */
    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    /**
     * Resolves the 'categoryBySlug' query.
     * Returns a single category by its slug.
     *
     * @param slug the category slug (e.g., "news", "opinion")
     * @return the category if found, null otherwise
     */
    public Category categoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug).orElse(null);
    }
}

