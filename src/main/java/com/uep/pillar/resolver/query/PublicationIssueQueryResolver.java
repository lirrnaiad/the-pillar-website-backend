package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.service.ArticleService;
import com.uep.pillar.service.PublicationIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for PublicationIssue-related queries.
 */
@Component
@RequiredArgsConstructor
public class PublicationIssueQueryResolver {

    private final PublicationIssueService publicationIssueService;
    private final ArticleService articleService;

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
        // Use a reasonable page size limit instead of Integer.MAX_VALUE
        // Return published articles for this issue, ordered by published date (newest first)
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> articlePage = articleService.findPublishedByIssueId(publicationIssue.getId(), pageable);
        return articlePage.getContent();
    }
}

