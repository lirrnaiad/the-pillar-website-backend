package com.uep.pillar.resolver.query;

import com.uep.pillar.dto.ArticleEdge;
import com.uep.pillar.dto.ArticlesConnection;
import com.uep.pillar.dto.PageInfo;
import com.uep.pillar.model.Article;
import com.uep.pillar.service.ArticleService;
import com.uep.pillar.service.SearchService;
import com.uep.pillar.dto.SearchHighlight;
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
    private final SearchService searchService;

    /**
     * Search articles using full-text search.
     * 
     * @param query the search query string
     * @param first maximum number of results to return
     * @param after cursor for pagination (base64 encoded page number)
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
                // Extract page number from cursor (format: "page_N")
                String pageStr = decoded.replace("page_", "");
                pageNumber = Integer.parseInt(pageStr);
            } catch (Exception e) {
                // Invalid cursor, start from beginning
                pageNumber = 0;
            }
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Article> page = searchService.searchPublished(query, pageable);

        // Convert to edges - each article gets a unique cursor based on its ID
        List<ArticleEdge> edges = page.getContent().stream()
                .map(article -> ArticleEdge.builder()
                        .node(article)
                        .cursor(encodeCursor(article.getId()))
                        .build())
                .collect(Collectors.toList());

        // Build page info
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
        
        // endCursor: cursor for pagination (page-based for next page navigation)
        // This is what clients should pass as 'after' to get the next page
        String endCursor = page.hasNext()
                ? Base64.getEncoder().encodeToString(("page_" + (pageNumber + 1)).getBytes())
                : (edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor());

        PageInfo pageInfo = PageInfo.builder()
                .hasNextPage(page.hasNext())
                .hasPreviousPage(page.hasPrevious())
                .startCursor(startCursor)
                .endCursor(endCursor)
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

    /**
     * Search with highlighted snippets suitable for preview rendering.
     */
    public List<SearchHighlight> searchHighlights(String query, Integer limit) {
        int max = limit != null ? limit : 10;
        return searchService.searchWithHighlights(query, max);
    }
}

