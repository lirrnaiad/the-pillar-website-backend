package com.uep.pillar.repository;

import com.uep.pillar.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Tag entity operations.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    /**
     * Find a tag by its URL slug.
     * 
     * @param slug the tag slug
     * @return the tag if found
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * Find a tag by its name.
     * 
     * @param name the tag name
     * @return the tag if found
     */
    Optional<Tag> findByName(String name);

    /**
     * Find multiple tags by their names.
     * Useful for bulk tag operations when creating/updating articles.
     * 
     * @param names collection of tag names
     * @return list of matching tags
     */
    List<Tag> findByNameIn(Collection<String> names);

    /**
     * Find multiple tags by their slugs.
     * 
     * @param slugs collection of tag slugs
     * @return list of matching tags
     */
    List<Tag> findBySlugIn(Collection<String> slugs);

    /**
     * Check if a tag with the given slug exists.
     * 
     * @param slug the slug to check
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if a tag with the given name exists.
     * 
     * @param name the name to check
     * @return true if exists
     */
    boolean existsByName(String name);
}

