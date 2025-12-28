package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated articles result with cursor-based pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesConnection {
    private List<ArticleEdge> edges;
    private PageInfo pageInfo;
    private Integer totalCount;
}

