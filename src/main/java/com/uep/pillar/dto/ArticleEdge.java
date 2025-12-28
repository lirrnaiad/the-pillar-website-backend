package com.uep.pillar.dto;

import com.uep.pillar.model.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Article edge for pagination.
 * Contains the article node and cursor for fetching next/previous page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEdge {
    private Article node;
    private String cursor;
}

