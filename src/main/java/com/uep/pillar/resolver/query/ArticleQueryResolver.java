package com.uep.pillar.resolver.query;

import com.uep.pillar.dto.ArticleEdge;
import com.uep.pillar.dto.ArticlesConnection;
import com.uep.pillar.dto.ArticleFilter;
import com.uep.pillar.dto.ArticleSort;
import com.uep.pillar.dto.PageInfo;
import com.uep.pillar.model.Article;
import com.uep.pillar.service.ArticleService;
import com.uep.pillar.repository.ArticleRepository;
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
    private final ArticleRepository articleRepository; // Still needed for complex queries

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

        // Build query based on filter
        Page<Article> articlePage = buildQuery(filter, pageable);

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
        Page<Article> articlePage = articleRepository.findPublishedByCategorySlug(
                categorySlug, 
                pageable
        );

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
        Page<Article> featuredPage = articleRepository.findTopFeatured(pageable);
        return featuredPage.getContent();
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
     * Build query based on filter and execute it.
     * NOTE: Currently only supports single filter conditions. Multiple filters applied simultaneously
     * are not yet supported and will use only the first matching condition in priority order.
     * Priority order: status > categoryId > authorId > featured > search > issueId > tagIds
     */
    private Page<Article> buildQuery(ArticleFilter filter, Pageable pageable) {
        if (filter == null) {
            // No filter, return all articles
            return articleRepository.findAll(pageable);
        }

        // Filter by status (highest priority)
        if (filter.getStatus() != null) {
            return articleRepository.findByStatus(filter.getStatus(), pageable);
        }

        // Filter by category
        if (filter.getCategoryId() != null) {
            return articleRepository.findByCategoryId(filter.getCategoryId().intValue(), pageable);
        }

        // Filter by author
        if (filter.getAuthorId() != null) {
            return articleRepository.findByAuthorId(filter.getAuthorId(), pageable);
        }

        // Filter by featured
        if (filter.getFeatured() != null && filter.getFeatured()) {
            return articleRepository.findTopFeatured(pageable);
        }

        // Filter by search query
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            return articleRepository.search(filter.getSearch().trim(), pageable);
        }

        // Filter by issue
        if (filter.getIssueId() != null) {
            return articleRepository.findByIssueId(filter.getIssueId(), pageable);
        }

        // Filter by tags - return empty result if tag filtering is requested
        // This prevents returning all articles when tag filtering is expected
        if (filter.getTagIds() != null && !filter.getTagIds().isEmpty()) {
            // Tag filtering with multiple tag IDs is not yet implemented
            // Return empty result set to avoid confusion
            return Page.empty(pageable);
        }

        // No specific filter, return all
        return articleRepository.findAll(pageable);
    }

    /**
     * Build ArticlesConnection from Page result.
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
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
        String endCursor = edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor();

        // For next page cursor, encode next page number
        String nextPageCursor = articlePage.hasNext()
                ? Base64.getEncoder().encodeToString(("page_" + (currentPage + 1)).getBytes())
                : null;

        PageInfo pageInfo = PageInfo.builder()
                .hasNextPage(articlePage.hasNext())
                .hasPreviousPage(articlePage.hasPrevious())
                .startCursor(startCursor)
                .endCursor(nextPageCursor)
                .build();

        return ArticlesConnection.builder()
                .edges(edges)
                .pageInfo(pageInfo)
                .totalCount((int) articlePage.getTotalElements())
                .build();
    }
}

