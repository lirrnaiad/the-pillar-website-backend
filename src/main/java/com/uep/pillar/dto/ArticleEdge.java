package com.uep.pillar.dto;

import com.uep.pillar.model.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Article edge for pagination in GraphQL connections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEdge {
    private Article node;
    private String cursor;
}

