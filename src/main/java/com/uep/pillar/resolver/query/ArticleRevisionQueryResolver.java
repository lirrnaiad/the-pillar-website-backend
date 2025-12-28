package com.uep.pillar.resolver.query;

import com.uep.pillar.model.ArticleRevision;
import com.uep.pillar.service.ArticleRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for ArticleRevision queries.
 */
@Component
@RequiredArgsConstructor
public class ArticleRevisionQueryResolver {

    private final ArticleRevisionService articleRevisionService;

    /**
     * Get revisions for a given article.
     */
    public List<ArticleRevision> articleRevisions(String articleId) {
        try {
            Long id = Long.parseLong(articleId);
            return articleRevisionService.findByArticleId(id);
        } catch (NumberFormatException e) {
            return List.of();
        }
    }
}
