package com.uep.pillar.controller;

import com.uep.pillar.dto.ArticleStatistics;
import com.uep.pillar.dto.PublicationStatistics;
import com.uep.pillar.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for statistics operations.
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Get article statistics.
     * GET /api/statistics/articles
     */
    @GetMapping("/articles")
    public ResponseEntity<ArticleStatistics> getArticleStatistics() {
        return ResponseEntity.ok(statisticsService.getArticleStatistics());
    }

    /**
     * Get publication statistics.
     * GET /api/statistics/publication
     */
    @GetMapping("/publication")
    public ResponseEntity<PublicationStatistics> getPublicationStatistics() {
        return ResponseEntity.ok(statisticsService.getPublicationStatistics());
    }
}

