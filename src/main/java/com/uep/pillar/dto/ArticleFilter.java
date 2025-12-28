package com.uep.pillar.dto;

import com.uep.pillar.model.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Input for article filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFilter {
    private ArticleStatus status;
    private Long categoryId;
    private List<Long> tagIds;
    private Long issueId;
    private Long authorId;
    private Boolean featured;
    private String search;
}

