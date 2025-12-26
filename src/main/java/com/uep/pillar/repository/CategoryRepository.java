package com.uep.pillar.repository;

import com.uep.pillar.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Find a category by its URL slug.
     * 
     * @param slug the category slug (e.g., "news", "opinion")
     * @return the category if found
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Find a category by its name.
     * 
     * @param name the category name (e.g., "News", "Opinion")
     * @return the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Check if a category with the given slug exists.
     * 
     * @param slug the slug to check
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if a category with the given name exists.
     * 
     * @param name the name to check
     * @return true if exists
     */
    boolean existsByName(String name);
}

