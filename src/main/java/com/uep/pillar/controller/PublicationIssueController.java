package com.uep.pillar.controller;

import com.uep.pillar.dto.CreatePublicationIssueInput;
import com.uep.pillar.dto.UpdatePublicationIssueInput;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.service.PublicationIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for publication issue management operations.
 */
@RestController
@RequestMapping("/api/publication-issues")
@RequiredArgsConstructor
public class PublicationIssueController {

    private final PublicationIssueService publicationIssueService;

    /**
     * Get all published publication issues.
     * GET /api/publication-issues
     */
    @GetMapping
    public ResponseEntity<List<PublicationIssue>> getAllPublicationIssues() {
        return ResponseEntity.ok(publicationIssueService.listPublished());
    }

    /**
     * Get publication issue by ID.
     * GET /api/publication-issues/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PublicationIssue> getPublicationIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(publicationIssueService.findById(id));
    }

    /**
     * Get publication issue by slug.
     * GET /api/publication-issues/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PublicationIssue> getPublicationIssueBySlug(@PathVariable String slug) {
        return publicationIssueService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new publication issue.
     * POST /api/publication-issues
     */
    @PostMapping
    public ResponseEntity<PublicationIssue> createPublicationIssue(@RequestBody CreatePublicationIssueInput input) {
        PublicationIssue issue = publicationIssueService.create(
                input.getTitle(),
                input.getDescription(),
                input.getCoverUrl()
        );

        // If publishedAt is provided, publish the issue
        if (input.getPublishedAt() != null) {
            issue = publicationIssueService.publish(issue.getId(), input.getPublishedAt());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    /**
     * Update an existing publication issue.
     * PUT /api/publication-issues/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PublicationIssue> updatePublicationIssue(
            @PathVariable Long id,
            @RequestBody UpdatePublicationIssueInput input) {
        PublicationIssue issue = publicationIssueService.update(
                id,
                input.getTitle(),
                input.getDescription(),
                input.getCoverUrl()
        );

        // Handle publishedAt if provided
        if (input.getPublishedAt() != null) {
            issue = publicationIssueService.publish(id, input.getPublishedAt());
        }

        return ResponseEntity.ok(issue);
    }

    /**
     * Delete a publication issue.
     * DELETE /api/publication-issues/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublicationIssue(@PathVariable Long id) {
        publicationIssueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

