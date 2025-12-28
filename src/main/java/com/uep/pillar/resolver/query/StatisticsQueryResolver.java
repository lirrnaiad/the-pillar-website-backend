package com.uep.pillar.resolver.query;

import com.uep.pillar.dto.ArticleStatistics;
import com.uep.pillar.dto.PublicationStatistics;
import com.uep.pillar.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GraphQL Query Resolver for statistics queries.
 */
@Component
@RequiredArgsConstructor
public class StatisticsQueryResolver {

    private final StatisticsService statisticsService;

    public ArticleStatistics articleStats() {
        return statisticsService.getArticleStatistics();
    }

    public PublicationStatistics publicationStats() {
        return statisticsService.getPublicationStatistics();
    }
}
