package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.PublicationIssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for PublicationIssue-related queries.
 */
@Component
public class PublicationIssueQueryResolver {

    private final PublicationIssueRepository publicationIssueRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public PublicationIssueQueryResolver(
            PublicationIssueRepository publicationIssueRepository,
            ArticleRepository articleRepository) {
        this.publicationIssueRepository = publicationIssueRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * Get all publication issues.
     * 
     * @return list of all publication issues
     */
    public List<PublicationIssue> publicationIssues() {
        return publicationIssueRepository.findAll();
    }

    /**
     * Get a publication issue by ID.
     * 
     * @param id the issue ID
     * @return the publication issue if found, null otherwise
     */
    public PublicationIssue publicationIssue(String id) {
        try {
            Long issueId = Long.parseLong(id);
            return publicationIssueRepository.findById(issueId).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get a publication issue by its slug.
     * 
     * @param slug the issue slug
     * @return the publication issue if found, null otherwise
     */
    public PublicationIssue publicationIssueBySlug(String slug) {
        return publicationIssueRepository.findBySlug(slug).orElse(null);
    }

    /**
     * Resolve the articles field for PublicationIssue.
     * This method is called by GraphQL when querying the articles field on PublicationIssue.
     * Returns only published articles for the given issue.
     * 
     * @param publicationIssue the publication issue
     * @return list of published articles in this issue
     */
    public List<Article> articles(PublicationIssue publicationIssue) {
        if (publicationIssue == null || publicationIssue.getId() == null) {
            return List.of();
        }
        // Return published articles for this issue, ordered by published date (newest first)
        return articleRepository.findByIssueId(publicationIssue.getId(), 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .stream()
                .filter(article -> article.getStatus() == com.uep.pillar.model.enums.ArticleStatus.PUBLISHED)
                .sorted((a1, a2) -> {
                    if (a1.getPublishedAt() == null && a2.getPublishedAt() == null) return 0;
                    if (a1.getPublishedAt() == null) return 1;
                    if (a2.getPublishedAt() == null) return -1;
                    return a2.getPublishedAt().compareTo(a1.getPublishedAt());
                })
                .toList();
    }
}

