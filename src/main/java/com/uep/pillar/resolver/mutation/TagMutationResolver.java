package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateTagInput;
import com.uep.pillar.dto.UpdateTagInput;
import com.uep.pillar.model.Tag;
import com.uep.pillar.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for Tag mutations.
 * Handles tag creation, updates, and deletion.
 */
@Component
@RequiredArgsConstructor
public class TagMutationResolver {

    private final TagService tagService;

    /**
     * Create a new tag.
     *
     * @param input tag creation input
     * @return the created tag
     */
    public Tag createTag(CreateTagInput input) {
        // Note: Slug from input is ignored; service auto-generates from name
        return tagService.create(input.getName());
    }

    /**
     * Update an existing tag.
     *
     * @param input tag update input
     * @return the updated tag
     */
    public Tag updateTag(UpdateTagInput input) {
        Integer id = parseIntegerId(input.getId(), "Tag ID");
        
        // Note: Slug from input is ignored; service auto-updates if name changes
        return tagService.update(id, input.getName());
    }

    /**
     * Delete a tag.
     *
     * @param id the tag ID
     * @return true on success
     */
    public Boolean deleteTag(String id) {
        Integer tagId = parseIntegerId(id, "Tag ID");
        tagService.delete(tagId);
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
