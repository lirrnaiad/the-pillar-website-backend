package com.uep.pillar.controller;

import com.uep.pillar.dto.CreateTagInput;
import com.uep.pillar.dto.UpdateTagInput;
import com.uep.pillar.model.Tag;
import com.uep.pillar.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for tag management operations.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * Get all tags.
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.findAll());
    }

    /**
     * Get tag by ID.
     * GET /api/tags/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Integer id) {
        return ResponseEntity.ok(tagService.findById(id));
    }

    /**
     * Get tag by slug.
     * GET /api/tags/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Tag> getTagBySlug(@PathVariable String slug) {
        return tagService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new tag.
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagInput input) {
        Tag tag = tagService.create(input.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    /**
     * Update an existing tag.
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable Integer id,
            @RequestBody UpdateTagInput input) {
        Tag tag = tagService.update(id, input.getName());
        return ResponseEntity.ok(tag);
    }

    /**
     * Delete a tag.
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

