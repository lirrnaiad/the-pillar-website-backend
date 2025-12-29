package com.uep.pillar.controller;

import com.uep.pillar.dto.SearchHighlight;
import com.uep.pillar.model.Article;
import com.uep.pillar.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for search operations.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Search articles using full-text search.
     * GET /api/search/articles
     */
    @GetMapping("/articles")
    public ResponseEntity<Page<Article>> searchArticles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Limit page size to max 100
        int pageSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Article> results = searchService.searchPublished(query, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Search with highlighted snippets suitable for preview rendering.
     * GET /api/search/highlights
     */
    @GetMapping("/highlights")
    public ResponseEntity<List<SearchHighlight>> searchHighlights(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        int max = Math.min(limit, 50); // Max 50 highlights
        List<SearchHighlight> highlights = searchService.searchWithHighlights(query, max);
        return ResponseEntity.ok(highlights);
    }
}

