package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Tag;
import com.uep.pillar.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for Tag-related queries.
 */
@Component
@RequiredArgsConstructor
public class TagQueryResolver {

    private final TagService tagService;

    /**
     * Get all tags.
     * 
     * @return list of all tags
     */
    public List<Tag> tags() {
        return tagService.findAll();
    }

    /**
     * Get a tag by ID.
     * 
     * @param id the tag ID
     * @return the tag if found, null otherwise
     */
    public Tag tag(String id) {
        try {
            Integer tagId = Integer.parseInt(id);
            return tagService.findById(tagId);
        } catch (NumberFormatException | com.uep.pillar.exception.ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get a tag by its slug.
     * 
     * @param slug the tag slug
     * @return the tag if found, null otherwise
     */
    public Tag tagBySlug(String slug) {
        return tagService.findBySlug(slug).orElse(null);
    }
}

