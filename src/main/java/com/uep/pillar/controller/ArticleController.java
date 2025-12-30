package com.uep.pillar.controller;

import com.uep.pillar.dto.ArticleFilter;
import com.uep.pillar.dto.CreateArticleInput;
import com.uep.pillar.dto.UpdateArticleInput;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.Media;
import com.uep.pillar.model.Tag;
import com.uep.pillar.model.User;
import com.uep.pillar.service.ArticleRevisionService;
import com.uep.pillar.service.ArticleService;
import com.uep.pillar.service.MediaService;
import com.uep.pillar.service.TagService;
import com.uep.pillar.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for article management operations.
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleService articleService;
    private final TagService tagService;
    private final MediaService mediaService;
    private final ArticleRevisionService articleRevisionService;
    private final UserService userService;

    @Data
    public static class RejectRequest {
        private String reason;
    }

    @Data
    public static class FeaturedRequest {
        private Boolean featured;
    }

    /**
     * Get all articles with optional filtering, sorting, and pagination.
     * GET /api/articles
     */
    @GetMapping
    public ResponseEntity<Page<Article>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Long issueId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {
        
        // #region agent log
        writeDebugLog("ArticleController:getArticles:ENTRY", 
            "getArticles called with params",
            "A,B,D,E",
            String.format("{\"page\":%d,\"size\":%d,\"status\":\"%s\",\"categoryId\":\"%s\",\"featured\":\"%s\"}", 
                page, size, status, categoryId, featured));
        // #endregion
        
        int pageSize = Math.min(size, 100); // Max 100 items per page
        Sort sort = buildSort(sortField, sortDirection);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        ArticleFilter filter = ArticleFilter.builder()
                .status(status != null ? com.uep.pillar.model.enums.ArticleStatus.valueOf(status) : null)
                .categoryId(categoryId)
                .tagIds(tagIds)
                .issueId(issueId)
                .authorId(authorId)
                .featured(featured)
                .search(search)
                .build();

        // #region agent log
        writeDebugLog("ArticleController:getArticles:BEFORE_QUERY", 
            "About to call articleService.findWithFilter",
            "B,D",
            String.format("{\"filter\":\"%s\",\"pageable\":\"%s\"}", filter, pageable));
        // #endregion

        Page<Article> articlePage = articleService.findWithFilter(filter, pageable);
        
        // #region agent log
        writeDebugLog("ArticleController:getArticles:AFTER_QUERY", 
            "Query completed successfully",
            "B,D",
            String.format("{\"totalElements\":%d,\"totalPages\":%d,\"numberOfElements\":%d}", 
                articlePage.getTotalElements(), articlePage.getTotalPages(), articlePage.getNumberOfElements()));
        // #endregion

        return ResponseEntity.ok(articlePage);
    }

    /**
     * Get article by ID.
     * GET /api/articles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.findById(id));
    }

    /**
     * Get article by slug.
     * GET /api/articles/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Article> getArticleBySlug(@PathVariable String slug) {
        return articleService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get featured articles.
     * GET /api/articles/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Article>> getFeaturedArticles(
            @RequestParam(defaultValue = "5") int limit) {
        // #region agent log
        writeDebugLog("ArticleController:getFeaturedArticles:ENTRY", 
            "getFeaturedArticles called",
            "A,B,C,D",
            String.format("{\"limit\":%d}", limit));
        // #endregion
        
        int maxLimit = Math.min(limit, 50);
        Pageable pageable = PageRequest.of(0, maxLimit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> featuredPage = articleService.findFeatured(pageable);
        
        // #region agent log
        writeDebugLog("ArticleController:getFeaturedArticles:AFTER_QUERY", 
            "Query completed - checking serialization",
            "A,B,C",
            String.format("{\"resultCount\":%d,\"hasContent\":%b}", featuredPage.getNumberOfElements(), !featuredPage.getContent().isEmpty()));
        // #endregion
        
        return ResponseEntity.ok(featuredPage.getContent());
    }

    /**
     * Get recent published articles.
     * GET /api/articles/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Article>> getRecentArticles(
            @RequestParam(defaultValue = "10") int limit) {
        // #region agent log
        writeDebugLog("ArticleController:getRecentArticles:ENTRY", 
            "getRecentArticles called",
            "A,B,C,D",
            String.format("{\"limit\":%d}", limit));
        // #endregion
        
        int maxLimit = Math.min(limit, 50);
        Pageable pageable = PageRequest.of(0, maxLimit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> recentPage = articleService.findRecentPublished(pageable);
        
        // #region agent log
        writeDebugLog("ArticleController:getRecentArticles:AFTER_QUERY", 
            "Query completed - checking serialization",
            "A,B,C",
            String.format("{\"resultCount\":%d,\"hasContent\":%b}", recentPage.getNumberOfElements(), !recentPage.getContent().isEmpty()));
        // #endregion
        
        return ResponseEntity.ok(recentPage.getContent());
    }

    /**
     * Get articles by category slug.
     * GET /api/articles/category/{categorySlug}
     */
    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<Page<Article>> getArticlesByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {
        // #region agent log
        writeDebugLog("ArticleController:getArticlesByCategory:ENTRY", 
            "getArticlesByCategory called",
            "A,B,C,D",
            String.format("{\"categorySlug\":\"%s\",\"page\":%d,\"size\":%d}", categorySlug, page, size));
        // #endregion
        
        int pageSize = Math.min(size, 100);
        Sort sort = buildSort(sortField, sortDirection);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Article> articlePage = articleService.findPublishedByCategorySlug(categorySlug, pageable);
        
        // #region agent log
        writeDebugLog("ArticleController:getArticlesByCategory:AFTER_QUERY", 
            "Query completed - checking serialization",
            "A,B,C",
            String.format("{\"categorySlug\":\"%s\",\"resultCount\":%d}", categorySlug, articlePage.getNumberOfElements()));
        // #endregion
        
        return ResponseEntity.ok(articlePage);
    }

    /**
     * Create a new article.
     * POST /api/articles
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','WRITER')")
    public ResponseEntity<Article> createArticle(@RequestBody CreateArticleInput input) {
        User author = getCurrentUser();
        if (author == null) {
            throw new IllegalStateException("Authentication required to create article. User not found in security context.");
        }

        Integer categoryId = input.getCategoryId() != null 
            ? parseIntegerId(input.getCategoryId(), "Category ID") 
            : null;
        Long issueId = input.getIssueId() != null 
            ? parseLongId(input.getIssueId(), "Issue ID") 
            : null;
        Set<Integer> tagIds = input.getTagIds() != null 
            ? input.getTagIds().stream()
                .map(id -> parseIntegerId(id, "Tag ID"))
                .collect(Collectors.toSet())
            : Collections.emptySet();

        Article article = articleService.create(
            input.getTitle(),
            input.getContent(),
            input.getExcerpt(),
            author,
            categoryId,
            issueId,
            tagIds,
            input.getSlug()
        );

        applyMetadataFields(article, input.getCoverId(), input.getMetaTitle(), 
                           input.getMetaDescription(), input.getOgImage());

        if (input.getStatus() != null && input.getStatus() != com.uep.pillar.model.enums.ArticleStatus.DRAFT) {
            switch (input.getStatus()) {
                case PENDING_REVIEW:
                    article = articleService.submitForReview(article.getId());
                    break;
                case PUBLISHED:
                    article = articleService.submitForReview(article.getId());
                    article = articleService.publish(article.getId());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid initial status: " + input.getStatus());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(article);
    }

    /**
     * Update an existing article.
     * PUT /api/articles/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','WRITER')")
    public ResponseEntity<Article> updateArticle(
            @PathVariable Long id,
            @RequestBody UpdateArticleInput input) {
        User editor = getCurrentUser();
        Article current = articleService.findById(id);
        articleRevisionService.saveSnapshot(current, editor, null);

        Integer categoryId = input.getCategoryId() != null 
            ? parseIntegerId(input.getCategoryId(), "Category ID") 
            : null;
        Long issueId = input.getIssueId() != null 
            ? parseLongId(input.getIssueId(), "Issue ID") 
            : null;
        Set<Integer> tagIds = input.getTagIds() != null 
            ? input.getTagIds().stream()
                .map(tagId -> parseIntegerId(tagId, "Tag ID"))
                .collect(Collectors.toSet())
            : null;

        Article article = articleService.update(
            id,
            input.getTitle(),
            input.getContent(),
            input.getExcerpt(),
            categoryId,
            issueId,
            tagIds,
            input.getSlug()
        );

        applyMetadataFields(article, input.getCoverId(), input.getMetaTitle(), 
                           input.getMetaDescription(), input.getOgImage());

        if (input.getFeatured() != null && input.getFeatured() != article.isFeatured()) {
            article = articleService.setFeatured(id, input.getFeatured());
        }

        if (input.getStatus() != null && input.getStatus() != article.getStatus()) {
            article = handleStatusTransition(id, input.getStatus());
        }

        return ResponseEntity.ok(article);
    }

    /**
     * Delete an article (soft delete).
     * DELETE /api/articles/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish an article.
     * POST /api/articles/{id}/publish
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> publishArticle(@PathVariable Long id) {
        Article article = articleService.publish(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Archive an article.
     * POST /api/articles/{id}/archive
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> archiveArticle(@PathVariable Long id) {
        Article article = articleService.archive(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Reject an article with feedback.
     * POST /api/articles/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> rejectArticle(
            @PathVariable Long id,
            @RequestBody RejectRequest request) {
        Article article = articleService.reject(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Restore a soft-deleted article.
     * POST /api/articles/{id}/restore
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> restoreArticle(@PathVariable Long id) {
        articleService.restore(id);
        Article article = articleService.findById(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Increment article view count.
     * POST /api/articles/{id}/views
     */
    @PostMapping("/{id}/views")
    public ResponseEntity<Article> incrementArticleViews(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        Article article = articleService.findById(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Toggle article featured status.
     * PUT /api/articles/{id}/featured
     */
    @PutMapping("/{id}/featured")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> toggleFeatured(
            @PathVariable Long id,
            @RequestBody FeaturedRequest request) {
        Article article = articleService.setFeatured(id, request.getFeatured());
        return ResponseEntity.ok(article);
    }

    /**
     * Add tag to article.
     * POST /api/articles/{id}/tags
     */
    @PostMapping("/{id}/tags")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> addArticleTag(
            @PathVariable Long id,
            @RequestParam Long tagId) {
        Tag tag = tagService.findById(tagId.intValue());
        Article article = articleService.addTagsBySlugs(id, Collections.singletonList(tag.getSlug()));
        return ResponseEntity.ok(article);
    }

    /**
     * Remove tag from article.
     * DELETE /api/articles/{id}/tags/{tagId}
     */
    @DeleteMapping("/{id}/tags/{tagId}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> removeArticleTag(
            @PathVariable Long id,
            @PathVariable Integer tagId) {
        Tag tag = tagService.findById(tagId);
        Article article = articleService.removeTagsBySlugs(id, Collections.singletonList(tag.getSlug()));
        return ResponseEntity.ok(article);
    }

    // ==================== HELPER METHODS ====================

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userService.findByEmail(email).orElse(null);
        }
        
        return null;
    }

    private Long parseLongId(String id, String fieldName) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    private Integer parseIntegerId(String id, String fieldName) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    private Sort buildSort(String sortField, String sortDirection) {
        if (sortField == null) {
            return Sort.by(Sort.Direction.DESC, "publishedAt");
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;

        String fieldName = switch (sortField.toUpperCase()) {
            case "TITLE" -> "title";
            case "CREATED_AT" -> "createdAt";
            case "UPDATED_AT" -> "updatedAt";
            case "PUBLISHED_AT" -> "publishedAt";
            case "VIEW_COUNT" -> "viewCount";
            default -> "publishedAt";
        };

        return Sort.by(direction, fieldName);
    }

    private void applyMetadataFields(Article article, String coverId, 
                                        String metaTitle, String metaDescription, String ogImage) {
        if (coverId != null) {
            Long coverMediaId = parseLongId(coverId, "Cover ID");
            Media cover = mediaService.findById(coverMediaId);
            article.setCover(cover);
        }

        if (metaTitle != null) {
            article.setMetaTitle(metaTitle);
        }
        if (metaDescription != null) {
            article.setMetaDescription(metaDescription);
        }
        if (ogImage != null) {
            article.setOgImage(ogImage);
        }
    }

    private Article handleStatusTransition(Long id, com.uep.pillar.model.enums.ArticleStatus target) {
        switch (target) {
            case DRAFT:
                return articleService.revertToDraft(id);
            case PENDING_REVIEW:
                return articleService.submitForReview(id);
            case PUBLISHED:
                return articleService.publish(id);
            case ARCHIVED:
                return articleService.archive(id);
            case REJECTED:
                return articleService.reject(id);
            default:
                throw new IllegalArgumentException("Unknown article status: " + target);
        }
    }

    // #region agent log helpers
    private void writeDebugLog(String location, String message, String hypothesisId, String dataJson) {
        try (FileWriter fw = new FileWriter("/home/lirrnaiad/Documents/Cursor/the-pillar/.cursor/debug.log", true)) {
            String logEntry = String.format("{\"location\":\"%s\",\"message\":\"%s\",\"data\":%s,\"timestamp\":%d,\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"%s\"}\n",
                location, message, dataJson, System.currentTimeMillis(), hypothesisId);
            fw.write(logEntry);
        } catch (IOException e) {
            log.warn("Failed to write debug log: {}", e.getMessage());
        }
    }
    // #endregion
}

