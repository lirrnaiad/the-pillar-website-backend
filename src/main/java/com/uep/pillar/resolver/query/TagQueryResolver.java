package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Tag;
import com.uep.pillar.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for Tag-related queries.
 */
@Component
public class TagQueryResolver {

    private final TagRepository tagRepository;

    @Autowired
    public TagQueryResolver(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Get all tags.
     * 
     * @return list of all tags
     */
    public List<Tag> tags() {
        return tagRepository.findAll();
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
            return tagRepository.findById(tagId).orElse(null);
        } catch (NumberFormatException e) {
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
        return tagRepository.findBySlug(slug).orElse(null);
    }
}

