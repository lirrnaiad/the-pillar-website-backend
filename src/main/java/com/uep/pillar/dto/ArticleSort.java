package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input for article sorting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSort {
    private ArticleSortField field;
    private SortDirection direction;

    /**
     * Article sort field enumeration.
     */
    public enum ArticleSortField {
        TITLE,
        CREATED_AT,
        UPDATED_AT,
        PUBLISHED_AT,
        VIEW_COUNT
    }

    /**
     * Sort direction enumeration.
     */
    public enum SortDirection {
        ASC,
        DESC
    }
}

