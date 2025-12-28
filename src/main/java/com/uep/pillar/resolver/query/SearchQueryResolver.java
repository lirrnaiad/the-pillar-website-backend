package com.uep.pillar.resolver.query;

import com.uep.pillar.dto.ArticleEdge;
import com.uep.pillar.dto.ArticlesConnection;
import com.uep.pillar.dto.PageInfo;
import com.uep.pillar.model.Article;
import com.uep.pillar.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphQL Query Resolver for search-related queries.
 */
@Component
@RequiredArgsConstructor
public class SearchQueryResolver {

    private final ArticleService articleService;

    /**
     * Search articles using full-text search.
     * 
     * @param query the search query string
     * @param first maximum number of results to return
     * @param after cursor for pagination (base64 encoded article ID)
     * @return paginated articles connection
     */
    public ArticlesConnection searchArticles(String query, Integer first, String after) {
        // Default to 10 if first is not provided
        int pageSize = first != null ? first : 10;
        
        // Parse cursor if provided
        int pageNumber = 0;
        if (after != null && !after.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(after));
                // Assuming cursor format is "article_{id}" or just the ID
                String idStr = decoded.replace("article_", "");
                // For simplicity, we'll use page-based pagination
                // In a production system, you might want cursor-based pagination
                pageNumber = Integer.parseInt(idStr) / pageSize;
            } catch (Exception e) {
                // Invalid cursor, start from beginning
                pageNumber = 0;
            }
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Article> page = articleService.searchPublished(query, pageable);

        // Convert to edges
        List<ArticleEdge> edges = page.getContent().stream()
                .map(article -> ArticleEdge.builder()
                        .node(article)
                        .cursor(encodeCursor(article.getId()))
                        .build())
                .collect(Collectors.toList());

        // Build page info
        PageInfo pageInfo = PageInfo.builder()
                .hasNextPage(page.hasNext())
                .hasPreviousPage(page.hasPrevious())
                .startCursor(edges.isEmpty() ? null : edges.get(0).getCursor())
                .endCursor(edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor())
                .build();

        return ArticlesConnection.builder()
                .edges(edges)
                .pageInfo(pageInfo)
                .totalCount((int) page.getTotalElements())
                .build();
    }

    /**
     * Encode article ID as cursor.
     */
    private String encodeCursor(Long articleId) {
        return Base64.getEncoder().encodeToString(("article_" + articleId).getBytes());
    }
}

