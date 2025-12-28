package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.service.PublicationIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for PublicationIssue-related queries.
 */
@Component
@RequiredArgsConstructor
public class PublicationIssueQueryResolver {

    private final PublicationIssueService publicationIssueService;
    private final ArticleRepository articleRepository;

    /**
     * Get all published publication issues.
     * 
     * @return list of published publication issues
     */
    public List<PublicationIssue> publicationIssues() {
        return publicationIssueService.listPublished();
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
            return publicationIssueService.findById(issueId);
        } catch (NumberFormatException | com.uep.pillar.exception.ResourceNotFoundException e) {
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
        return publicationIssueService.findBySlug(slug).orElse(null);
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

