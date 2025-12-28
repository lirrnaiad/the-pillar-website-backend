package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateArticleInput;
import com.uep.pillar.dto.UpdateArticleInput;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.Media;
import com.uep.pillar.model.Tag;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.MediaRepository;
import com.uep.pillar.service.ArticleService;
import com.uep.pillar.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GraphQL Mutation Resolver for Article mutations.
 * Handles article creation, updates, deletion, status transitions, and tag operations.
 */
@Component
@RequiredArgsConstructor
public class ArticleMutationResolver extends BaseMutationResolver {

    private final ArticleService articleService;
    private final TagService tagService;
    private final MediaRepository mediaRepository;
    private final ArticleRepository articleRepository;

    /**
     * Create a new article.
     *
     * @param input article creation input
     * @return the created article
     */
    public Article createArticle(CreateArticleInput input) {
        // Get current authenticated user as author
        User author = getCurrentUser();
        if (author == null) {
            throw new IllegalStateException("Authentication required to create article");
        }

        // Parse IDs
        Integer categoryId = input.getCategoryId() != null 
            ? parseIntegerId(input.getCategoryId(), "Category ID") 
            : null;
        Long issueId = input.getIssueId() != null 
            ? parseLongId(input.getIssueId(), "Issue ID") 
            : null;
        Set<Integer> tagIds = input.getTagIds() != null 
            ? input.getTagIds().stream()
                .map(id -> parseIntegerId(id, "Tag ID"))
                .collect(Collectors.toSet())
            : Collections.emptySet();

        // Create article (service always creates as DRAFT)
        Article article = articleService.create(
            input.getTitle(),
            input.getContent(),
            input.getExcerpt(),
            author,
            categoryId,
            issueId,
            tagIds,
            input.getSlug()
        );

        // Set cover image if provided
        if (input.getCoverId() != null) {
            Long coverId = parseLongId(input.getCoverId(), "Cover ID");
            Media cover = mediaRepository.findById(coverId)
                .orElseThrow(() -> new IllegalArgumentException("Cover media not found: " + coverId));
            article.setCover(cover);
        }

        // Set metadata fields if provided
        if (input.getMetaTitle() != null) {
            article.setMetaTitle(input.getMetaTitle());
        }
        if (input.getMetaDescription() != null) {
            article.setMetaDescription(input.getMetaDescription());
        }
        if (input.getOgImage() != null) {
            article.setOgImage(input.getOgImage());
        }

        // Save changes if any metadata was set
        if (input.getCoverId() != null || input.getMetaTitle() != null 
            || input.getMetaDescription() != null || input.getOgImage() != null) {
            article = articleRepository.save(article);
        }

        // Handle initial status if not DRAFT
        if (input.getStatus() != null && input.getStatus() != com.uep.pillar.model.enums.ArticleStatus.DRAFT) {
            switch (input.getStatus()) {
                case PENDING_REVIEW:
                    article = articleService.submitForReview(article.getId());
                    break;
                case PUBLISHED:
                    article = articleService.submitForReview(article.getId());
                    article = articleService.publish(article.getId());
                    break;
                default:
                    // REJECTED, ARCHIVED cannot be initial states
                    throw new IllegalArgumentException("Invalid initial status: " + input.getStatus());
            }
        }

        return article;
    }

    /**
     * Update an existing article.
     *
     * @param input article update input
     * @return the updated article
     */
    public Article updateArticle(UpdateArticleInput input) {
        Long id = parseLongId(input.getId(), "Article ID");

        // Parse optional IDs
        Integer categoryId = input.getCategoryId() != null 
            ? parseIntegerId(input.getCategoryId(), "Category ID") 
            : null;
        Long issueId = input.getIssueId() != null 
            ? parseLongId(input.getIssueId(), "Issue ID") 
            : null;
        Set<Integer> tagIds = input.getTagIds() != null 
            ? input.getTagIds().stream()
                .map(tagId -> parseIntegerId(tagId, "Tag ID"))
                .collect(Collectors.toSet())
            : null;

        // Update article
        Article article = articleService.update(
            id,
            input.getTitle(),
            input.getContent(),
            input.getExcerpt(),
            categoryId,
            issueId,
            tagIds,
            input.getSlug()
        );

        // Set cover image if provided
        if (input.getCoverId() != null) {
            Long coverId = parseLongId(input.getCoverId(), "Cover ID");
            Media cover = mediaRepository.findById(coverId)
                .orElseThrow(() -> new IllegalArgumentException("Cover media not found: " + coverId));
            article.setCover(cover);
        }

        // Set metadata fields if provided
        if (input.getMetaTitle() != null) {
            article.setMetaTitle(input.getMetaTitle());
        }
        if (input.getMetaDescription() != null) {
            article.setMetaDescription(input.getMetaDescription());
        }
        if (input.getOgImage() != null) {
            article.setOgImage(input.getOgImage());
        }

        // Save changes if any metadata was set
        if (input.getCoverId() != null || input.getMetaTitle() != null 
            || input.getMetaDescription() != null || input.getOgImage() != null) {
            article = articleRepository.save(article);
        }

        // Handle featured flag
        if (input.getFeatured() != null && input.getFeatured() != article.isFeatured()) {
            article = articleService.setFeatured(id, input.getFeatured());
        }

        // Handle status change if provided
        if (input.getStatus() != null && input.getStatus() != article.getStatus()) {
            article = handleStatusTransition(id, input.getStatus());
        }

        return article;
    }

    /**
     * Delete an article (soft delete).
     *
     * @param id the article ID
     * @return true on success
     */
    public Boolean deleteArticle(String id) {
        Long articleId = parseLongId(id, "Article ID");
        articleService.softDelete(articleId);
        return true;
    }

    /**
     * Publish an article.
     *
     * @param id the article ID
     * @return the published article
     */
    public Article publishArticle(String id) {
        Long articleId = parseLongId(id, "Article ID");
        return articleService.publish(articleId);
    }

    /**
     * Archive an article.
     *
     * @param id the article ID
     * @return the archived article
     */
    public Article archiveArticle(String id) {
        Long articleId = parseLongId(id, "Article ID");
        return articleService.archive(articleId);
    }

    /**
     * Reject an article with feedback.
     *
     * @param id the article ID
     * @param reason the rejection reason (for logging/audit, not stored in article)
     * @return the rejected article
     */
    public Article rejectArticle(String id, String reason) {
        Long articleId = parseLongId(id, "Article ID");
        // Note: Reason is accepted but not stored by service
        // Could be logged or sent to audit trail in future
        return articleService.reject(articleId);
    }

    /**
     * Restore a soft-deleted article.
     *
     * @param id the article ID
     * @return the restored article
     */
    public Article restoreArticle(String id) {
        Long articleId = parseLongId(id, "Article ID");
        articleService.restore(articleId);
        return articleService.findById(articleId);
    }

    /**
     * Increment article view count.
     *
     * @param id the article ID
     * @return the updated article
     */
    public Article incrementArticleViews(String id) {
        Long articleId = parseLongId(id, "Article ID");
        articleService.incrementViewCount(articleId);
        return articleService.findById(articleId);
    }

    /**
     * Toggle article featured status.
     *
     * @param id the article ID
     * @return the updated article
     */
    public Article toggleFeatured(String id) {
        Long articleId = parseLongId(id, "Article ID");
        Article article = articleService.findById(articleId);
        return articleService.setFeatured(articleId, !article.isFeatured());
    }

    /**
     * Add a tag to an article.
     *
     * @param articleId the article ID
     * @param tagId the tag ID
     * @return the updated article
     */
    public Article addArticleTag(String articleId, String tagId) {
        Long artId = parseLongId(articleId, "Article ID");
        Integer tId = parseIntegerId(tagId, "Tag ID");
        
        // Get tag slug (service uses slugs, not IDs)
        Tag tag = tagService.findById(tId);
        return articleService.addTagsBySlugs(artId, Collections.singletonList(tag.getSlug()));
    }

    /**
     * Remove a tag from an article.
     *
     * @param articleId the article ID
     * @param tagId the tag ID
     * @return the updated article
     */
    public Article removeArticleTag(String articleId, String tagId) {
        Long artId = parseLongId(articleId, "Article ID");
        Integer tId = parseIntegerId(tagId, "Tag ID");
        
        // Get tag slug (service uses slugs, not IDs)
        Tag tag = tagService.findById(tId);
        return articleService.removeTagsBySlugs(artId, Collections.singletonList(tag.getSlug()));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Handle status transitions between article states.
     */
    private Article handleStatusTransition(Long id, 
                                           com.uep.pillar.model.enums.ArticleStatus target) {
        switch (target) {
            case DRAFT:
                return articleService.revertToDraft(id);
            case PENDING_REVIEW:
                return articleService.submitForReview(id);
            case PUBLISHED:
                return articleService.publish(id);
            case ARCHIVED:
                return articleService.archive(id);
            case REJECTED:
                return articleService.reject(id);
            default:
                throw new IllegalArgumentException("Unknown article status: " + target);
        }
    }
}
