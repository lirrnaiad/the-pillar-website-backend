package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatistics {
    private int totalArticles;
    private int publishedArticles;
    private int draftArticles;
    private long totalViews;
    private float avgViewsPerArticle;
}
