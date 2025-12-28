package com.uep.pillar.resolver.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import com.uep.pillar.dto.ArticleEdge;
import com.uep.pillar.dto.ArticlesConnection;
import com.uep.pillar.dto.ArticleFilter;
import com.uep.pillar.dto.ArticleSort;
import com.uep.pillar.dto.PageInfo;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.Category;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GraphQL Query Resolver for Article queries.
 * Handles article retrieval with filtering, sorting, and pagination.
 */
@Component
public class ArticleQueryResolver implements GraphQLQueryResolver {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    public ArticleQueryResolver(ArticleRepository articleRepository, CategoryRepository categoryRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get a single article by slug (SEO-friendly URL).
     *
     * @param slug the article slug
     * @return the article if found, null otherwise
     */
    public Article articleBySlug(String slug) {
        return articleRepository.findBySlug(slug).orElse(null);
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
        if (after != null && !after.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(after));
                pageNumber = Integer.parseInt(decoded);
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
     * @return paginated articles connection
     */
    public ArticlesConnection articlesByCategory(String categorySlug, Integer first, String after) {
        // Default values
        int pageSize = first != null ? Math.min(first, 100) : 10;
        int pageNumber = 0;

        // Decode cursor if provided
        if (after != null && !after.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(after));
                pageNumber = Integer.parseInt(decoded);
            } catch (Exception e) {
                pageNumber = 0;
            }
        }

        // Find category by slug
        Optional<Category> categoryOpt = categoryRepository.findBySlug(categorySlug);
        if (categoryOpt.isEmpty()) {
            // Return empty connection if category not found
            return ArticlesConnection.builder()
                    .edges(List.of())
                    .pageInfo(PageInfo.builder()
                            .hasNextPage(false)
                            .hasPreviousPage(false)
                            .build())
                    .totalCount(0)
                    .build();
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, 
                Sort.by(Sort.Direction.DESC, "publishedAt"));

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
     */
    private Page<Article> buildQuery(ArticleFilter filter, Pageable pageable) {
        if (filter == null) {
            // No filter, return all articles
            return articleRepository.findAll(pageable);
        }

        // Filter by status
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

        // Filter by tags (requires custom query - simplified for now)
        // TODO: Implement tag filtering with proper query
        if (filter.getTagIds() != null && !filter.getTagIds().isEmpty()) {
            // For now, return all articles - tag filtering needs custom repository method
            return articleRepository.findAll(pageable);
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
                    String cursor = Base64.getEncoder().encodeToString(
                            String.valueOf(currentPage).getBytes()
                    );
                    return ArticleEdge.builder()
                            .node(article)
                            .cursor(cursor)
                            .build();
                })
                .collect(Collectors.toList());

        // Build page info
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();

        // For next page cursor, encode next page number
        String nextPageCursor = articlePage.hasNext()
                ? Base64.getEncoder().encodeToString(String.valueOf(currentPage + 1).getBytes())
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

