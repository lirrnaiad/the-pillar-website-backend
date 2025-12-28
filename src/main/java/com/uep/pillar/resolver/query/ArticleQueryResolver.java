package com.uep.pillar.resolver.query;

import com.uep.pillar.dto.ArticleEdge;
import com.uep.pillar.dto.ArticlesConnection;
import com.uep.pillar.dto.ArticleFilter;
import com.uep.pillar.dto.ArticleSort;
import com.uep.pillar.dto.PageInfo;
import com.uep.pillar.model.Article;
import com.uep.pillar.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphQL Query Resolver for Article queries.
 * Handles article retrieval with filtering, sorting, and pagination.
 */
@Component
@RequiredArgsConstructor
public class ArticleQueryResolver {

    private final ArticleService articleService;

    /**
     * Get a single article by slug (SEO-friendly URL).
     *
     * @param slug the article slug
     * @return the article if found, null otherwise
     */
    public Article articleBySlug(String slug) {
        return articleService.findBySlug(slug).orElse(null);
    }

    /**
     * Get a single article by ID.
     *
     * @param id the article ID
     * @return the article if found, null otherwise
     */
    public Article article(String id) {
        try {
            Long articleId = Long.parseLong(id);
            return articleService.findById(articleId);
        } catch (NumberFormatException | com.uep.pillar.exception.ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get all articles with optional filtering, sorting, and pagination.
     *
     * @param first number of articles to return (default: 10)
     * @param after cursor for pagination (for next page)
     * @param filter optional filter for articles
     * @param sort optional sort configuration
     * @return paginated articles connection
     */
    public ArticlesConnection articles(Integer first, String after, ArticleFilter filter, ArticleSort sort) {
        // Default values
        int pageSize = first != null ? Math.min(first, 100) : 10; // Max 100 items per page
        int pageNumber = 0;

        // Decode cursor to get page number if provided
        // Using page-based pagination for simplicity; cursor contains page number
        if (after != null && !after.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(after));
                // Extract page number from cursor
                String pageStr = decoded.replace("page_", "");
                pageNumber = Integer.parseInt(pageStr);
            } catch (Exception e) {
                // Invalid cursor, start from beginning
                pageNumber = 0;
            }
        }

        // Build sort specification
        Sort sortSpec = buildSort(sort);

        // Create pageable
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortSpec);

        // Delegate filtering to service layer
        Page<Article> articlePage = articleService.findWithFilter(filter, pageable);

        // Convert to connection
        return buildConnection(articlePage, pageNumber);
    }

    /**
     * Get articles by category slug.
     *
     * @param categorySlug the category slug
     * @param first number of articles to return (default: 10)
     * @param after cursor for pagination
     * @param sort optional sort configuration
     * @return paginated articles connection
     */
    public ArticlesConnection articlesByCategory(String categorySlug, Integer first, String after, ArticleSort sort) {
        // Default values
        int pageSize = first != null ? Math.min(first, 100) : 10;
        int pageNumber = 0;

        // Decode cursor if provided
        if (after != null && !after.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(after));
                // Extract page number from cursor
                String pageStr = decoded.replace("page_", "");
                pageNumber = Integer.parseInt(pageStr);
            } catch (Exception e) {
                pageNumber = 0;
            }
        }

        // Build sort specification
        Sort sortSpec = buildSort(sort);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortSpec);

        // Query published articles by category slug
        Page<Article> articlePage = articleService.findPublishedByCategorySlug(categorySlug, pageable);

        return buildConnection(articlePage, pageNumber);
    }

    /**
     * Get featured articles.
     *
     * @param first number of featured articles to return (default: 5)
     * @return list of featured articles
     */
    public List<Article> featuredArticles(Integer first) {
        int limit = first != null ? Math.min(first, 50) : 5; // Max 50 featured articles
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> featuredPage = articleService.findFeatured(pageable);
        return featuredPage.getContent();
    }

    /**
     * Get recent published articles.
     *
     * @param first number of articles to return (default: 10)
     * @return list of recent articles
     */
    public List<Article> recentArticles(Integer first) {
        int limit = first != null ? Math.min(first, 50) : 10; // Max 50 recent articles
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> recentPage = articleService.findRecentPublished(pageable);
        return recentPage.getContent();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Build sort specification from ArticleSort input.
     */
    private Sort buildSort(ArticleSort sort) {
        if (sort == null || sort.getField() == null) {
            // Default sort: published date descending
            return Sort.by(Sort.Direction.DESC, "publishedAt");
        }

        Sort.Direction direction = sort.getDirection() == ArticleSort.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String fieldName = switch (sort.getField()) {
            case TITLE -> "title";
            case CREATED_AT -> "createdAt";
            case UPDATED_AT -> "updatedAt";
            case PUBLISHED_AT -> "publishedAt";
            case VIEW_COUNT -> "viewCount";
        };

        return Sort.by(direction, fieldName);
    }

    /**
     * Build ArticlesConnection from Page result.
     * 
     * Note on cursor pagination:
     * - Each edge has a unique cursor based on article ID (for identification)
     * - PageInfo.endCursor contains the cursor for fetching the next page (page-based)
     * - This hybrid approach provides unique identifiers per article while using
     *   simpler page-based pagination for navigation
     */
    private ArticlesConnection buildConnection(Page<Article> articlePage, int currentPage) {
        List<ArticleEdge> edges = articlePage.getContent().stream()
                .map(article -> {
                    // Use article ID for cursor to make it unique per article
                    String cursor = Base64.getEncoder().encodeToString(
                            ("article_" + article.getId()).getBytes()
                    );
                    return ArticleEdge.builder()
                            .node(article)
                            .cursor(cursor)
                            .build();
                })
                .collect(Collectors.toList());

        // Build page info
        // startCursor: cursor of first edge (article ID based)
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
        
        // endCursor: cursor for pagination (page-based for next page navigation)
        // This is what clients should pass as 'after' to get the next page
        String endCursor = articlePage.hasNext()
                ? Base64.getEncoder().encodeToString(("page_" + (currentPage + 1)).getBytes())
                : (edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor());

        PageInfo pageInfo = PageInfo.builder()
                .hasNextPage(articlePage.hasNext())
                .hasPreviousPage(articlePage.hasPrevious())
                .startCursor(startCursor)
                .endCursor(endCursor)
                .build();

        return ArticlesConnection.builder()
                .edges(edges)
                .pageInfo(pageInfo)
                .totalCount((int) articlePage.getTotalElements())
                .build();
    }
}

