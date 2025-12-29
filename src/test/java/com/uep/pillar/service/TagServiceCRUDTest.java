package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Tag;
import com.uep.pillar.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRUD tests for TagService.
 * Tests create, read, update, and delete operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TagServiceCRUDTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void testCreateTag() {
        // CREATE
        Tag tag = tagService.create("Java");

        assertNotNull(tag);
        assertNotNull(tag.getId());
        assertEquals("Java", tag.getName());
        assertEquals("java", tag.getSlug());
    }

    @Test
    void testCreateTagAutoSlug() {
        // CREATE - slug should be auto-generated
        Tag tag = tagService.create("Spring Boot");

        assertEquals("Spring Boot", tag.getName());
        assertNotNull(tag.getSlug());
        assertTrue(tag.getSlug().contains("spring"));
        assertTrue(tag.getSlug().contains("boot"));
    }

    @Test
    void testReadTag() {
        // CREATE first
        Tag created = tagService.create("Read Test Tag");

        // READ by ID
        Tag found = tagService.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Read Test Tag", found.getName());

        // READ by slug
        Tag foundBySlug = tagService.findBySlug(created.getSlug())
                .orElseThrow();
        assertEquals(created.getId(), foundBySlug.getId());
    }

    @Test
    void testReadTagNotFound() {
        // READ non-existent tag
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.findById(99999);
        });
    }

    @Test
    void testUpdateTag() {
        // CREATE
        Tag tag = tagService.create("Original Name");

        Integer tagId = tag.getId();
        String originalSlug = tag.getSlug();

        // UPDATE
        Tag updated = tagService.update(tagId, "Updated Name");

        assertEquals(tagId, updated.getId());
        assertEquals("Updated Name", updated.getName());
        // Slug should change when name changes
        assertNotEquals(originalSlug, updated.getSlug());
    }

    @Test
    void testDeleteTag() {
        // CREATE
        Tag tag = tagService.create("To Be Deleted");

        Integer tagId = tag.getId();
        assertTrue(tagRepository.findById(tagId).isPresent());

        // DELETE
        tagService.delete(tagId);

        // Verify deleted
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.findById(tagId);
        });
    }

    @Test
    void testFindOrCreateTags() {
        // FIND OR CREATE - new tags
        List<Tag> tags = tagService.findOrCreateByNames(
                Arrays.asList("New Tag 1", "New Tag 2", "New Tag 3")
        );

        assertEquals(3, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("New Tag 1")));
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("New Tag 2")));
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("New Tag 3")));
    }

    @Test
    void testFindOrCreateTagsMixed() {
        // CREATE existing tag
        Tag existing = tagService.create("Existing Tag");

        // FIND OR CREATE - mix of existing and new
        List<Tag> tags = tagService.findOrCreateByNames(
                Arrays.asList("Existing Tag", "New Tag 1", "New Tag 2")
        );

        assertEquals(3, tags.size());
        // Should reuse existing tag
        assertTrue(tags.stream().anyMatch(t -> t.getId().equals(existing.getId())));
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("New Tag 1")));
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("New Tag 2")));
    }

    @Test
    void testListTags() {
        // CREATE multiple tags
        tagService.create("Tag 1");
        tagService.create("Tag 2");
        tagService.create("Tag 3");

        // LIST all
        List<Tag> allTags = tagService.findAll();
        assertTrue(allTags.size() >= 3);
    }
}

