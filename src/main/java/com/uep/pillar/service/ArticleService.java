package com.uep.pillar.service;

import com.uep.pillar.dto.ArticleFilter;
import com.uep.pillar.exception.InvalidStatusTransitionException;
import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.Category;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.model.Tag;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.ArticleStatus;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.CategoryRepository;
import com.uep.pillar.repository.PublicationIssueRepository;
import com.uep.pillar.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final SlugService slugService;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final PublicationIssueRepository publicationIssueRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
    }

    @Transactional(readOnly = true)
    public Optional<Article> findBySlug(String slug) {
        return articleRepository.findBySlug(slug);
    }

    @Transactional
    public Article create(String title,
                          String content,
                          String excerpt,
                          User author,
                          Integer categoryId,
                          Long issueId,
                          Set<Integer> tagIds,
                          String slugIfProvided) {
        String slug;
        if (slugIfProvided != null && !slugIfProvided.isBlank()) {
            String sanitized = slugService.sanitize(slugIfProvided);
            // Check uniqueness for manually provided slugs
            if (articleRepository.existsBySlug(sanitized)) {
                throw new com.uep.pillar.exception.SlugAlreadyExistsException(sanitized);
            }
            slug = sanitized;
        } else {
            slug = slugService.generateUniqueSlug(title, Article.class, null);
        }

        Article.ArticleBuilder builder = Article.builder()
                .title(title)
                .slug(slug)
                .content(content)
                .excerpt(excerpt)
                .author(author)
                .status(ArticleStatus.DRAFT)
                .featured(false)
                .viewCount(0L);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            builder.category(category);
        }
        if (issueId != null) {
            PublicationIssue issue = publicationIssueRepository.findById(issueId)
                    .orElseThrow(() -> new ResourceNotFoundException("PublicationIssue", issueId));
            builder.issue(issue);
        }
        Set<Tag> tags = new HashSet<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            // Fix N+1 query: use findAllById to fetch all tags in a single query
            List<Tag> foundTags = tagRepository.findAllById(tagIds);
            Set<Integer> foundIds = new HashSet<>();
            for (Tag tag : foundTags) {
                foundIds.add(tag.getId());
            }
            for (Integer tid : tagIds) {
                if (!foundIds.contains(tid)) {
                    throw new ResourceNotFoundException("Tag", tid);
                }
            }
            tags.addAll(foundTags);
        }
        builder.tags(tags);

        Article created = articleRepository.save(builder.build());
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.CREATE, null, created, null);
        return created;
    }

    @Transactional
    public Article update(Long id, String title, String content, String excerpt,
                          Integer categoryId, Long issueId, Collection<Integer> tagIds) {
        return update(id, title, content, excerpt, categoryId, issueId, tagIds, null);
    }

    @Transactional
    public Article update(Long id, String title, String content, String excerpt,
                          Integer categoryId, Long issueId, Collection<Integer> tagIds,
                          String slugIfProvided) {
        Article article = findById(id);
        // capture state before changes
        Article before = Article.builder()
            .id(article.getId())
            .title(article.getTitle())
            .slug(article.getSlug())
            .status(article.getStatus())
            .featured(article.isFeatured())
            .viewCount(article.getViewCount())
            .author(article.getAuthor())
            .category(article.getCategory())
            .issue(article.getIssue())
            .build();
        
        // Handle slug updates: explicit slug takes precedence, otherwise update on title change
        if (slugIfProvided != null && !slugIfProvided.trim().isEmpty()) {
            article.setSlug(slugService.generateUniqueSlug(slugIfProvided, Article.class, id));
        } else if (title != null && !title.equals(article.getTitle())) {
            article.setTitle(title);
            article.setSlug(slugService.generateUniqueSlug(title, Article.class, id));
        } else if (title != null) {
            article.setTitle(title);
        }
        
        if (content != null) article.setContent(content);
        if (excerpt != null) article.setExcerpt(excerpt);
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            article.setCategory(category);
        }
        if (issueId != null) {
            PublicationIssue issue = publicationIssueRepository.findById(issueId)
                    .orElseThrow(() -> new ResourceNotFoundException("PublicationIssue", issueId));
            article.setIssue(issue);
        }
        if (tagIds != null) {
            // Fix N+1 query: use findAllById to fetch all tags in a single query
            List<Tag> foundTags = tagRepository.findAllById(tagIds);
            Set<Integer> foundTagIds = new HashSet<>();
            for (Tag tag : foundTags) {
                foundTagIds.add(tag.getId());
            }
            for (Integer tid : tagIds) {
                if (!foundTagIds.contains(tid)) {
                    throw new ResourceNotFoundException("Tag", tid);
                }
            }
            article.setTags(new HashSet<>(foundTags));
        }
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, before, updated, null);
        return updated;
    }

    @Transactional
    public void softDelete(Long id) {
        // ensure exists first for consistent errors
        Article existing = findById(id);
        articleRepository.softDelete(existing.getId(), LocalDateTime.now());
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.DELETE, existing, null, null);
    }

    @Transactional
    public void restore(Long id) {
        // verify it exists possibly including deleted
        Article before = articleRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
        articleRepository.restore(id);
        Article restored = findById(id);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.RESTORE, before, restored, null);
    }

    @Transactional
    public Article setFeatured(Long id, boolean featured) {
        Article article = findById(id);
        article.setFeatured(featured);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, null, updated, "feature-toggle");
        return updated;
    }

    @Transactional
    public void incrementViewCount(Long id) {
        articleRepository.incrementViewCount(id);
    }

    @Transactional
    public Article submitForReview(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new InvalidStatusTransitionException("Only DRAFT can be submitted for review");
        }
        article.setStatus(ArticleStatus.PENDING_REVIEW);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, null, updated, "submit-for-review");
        return updated;
    }

    @Transactional
    public Article publish(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.DRAFT && article.getStatus() != ArticleStatus.PENDING_REVIEW) {
            throw new InvalidStatusTransitionException("Only DRAFT or PENDING_REVIEW can be published");
        }
        article.setStatus(ArticleStatus.PUBLISHED);
        // Only set publishedAt if it's null (first publication)
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.PUBLISH, null, updated, null);
        return updated;
    }

    @Transactional
    public Article reject(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.PENDING_REVIEW) {
            throw new InvalidStatusTransitionException("Only PENDING_REVIEW can be rejected");
        }
        article.setStatus(ArticleStatus.REJECTED);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, null, updated, "reject");
        return updated;
    }

    @Transactional
    public Article archive(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new InvalidStatusTransitionException("Only PUBLISHED can be archived");
        }
        article.setStatus(ArticleStatus.ARCHIVED);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.ARCHIVE, null, updated, null);
        return updated;
    }

    @Transactional
    public Article revertToDraft(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.PENDING_REVIEW
                && article.getStatus() != ArticleStatus.REJECTED) {
            throw new InvalidStatusTransitionException("Only PENDING_REVIEW or REJECTED can be reverted to DRAFT");
        }
        article.setStatus(ArticleStatus.DRAFT);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, null, updated, "revert-to-draft");
        return updated;
    }

    @Transactional(readOnly = true)
    public Page<Article> listPublished(Pageable pageable) {
        return articleRepository.findAllPublished(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findRecentPublished(Pageable pageable) {
        return articleRepository.findRecentPublished(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> searchPublished(String query, Pageable pageable) {
        return articleRepository.search(query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findByStatus(ArticleStatus status, Pageable pageable) {
        return articleRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findByCategoryId(Integer categoryId, Pageable pageable) {
        return articleRepository.findByCategoryId(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findByAuthorId(Long authorId, Pageable pageable) {
        return articleRepository.findByAuthorId(authorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findFeatured(Pageable pageable) {
        return articleRepository.findTopFeatured(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findPublishedByCategorySlug(String categorySlug, Pageable pageable) {
        return articleRepository.findPublishedByCategorySlug(categorySlug, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findPublishedByTagIds(Collection<Integer> tagIds, Pageable pageable) {
        return articleRepository.findPublishedByTagIds(tagIds, pageable);
    }

    @Transactional
    public Article addTagsBySlugs(Long articleId, Collection<String> slugs) {
        Article article = findById(articleId);
        List<Tag> tags = tagRepository.findBySlugIn(slugs);
        for (Tag t : tags) article.addTag(t);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, null, updated, "add-tags");
        return updated;
    }

    @Transactional
    public Article removeTagsBySlugs(Long articleId, Collection<String> slugs) {
        Article article = findById(articleId);
        List<Tag> tags = tagRepository.findBySlugIn(slugs);
        for (Tag t : tags) article.removeTag(t);
        Article updated = articleRepository.save(article);
        auditLogService.logArticle(com.uep.pillar.model.enums.AuditAction.UPDATE, null, updated, "remove-tags");
        return updated;
    }

    @Transactional(readOnly = true)
    public Page<Article> findPublishedByIssueId(Long issueId, Pageable pageable) {
        return articleRepository.findPublishedByIssueId(issueId, pageable);
    }

    /**
     * Centralized article filtering used by REST controllers.
     * Applies one filter at a time based on priority to avoid ambiguous combinations.
     * Priority order: status > categoryId > authorId > featured > search > issueId > tagIds.
     */
    @Transactional(readOnly = true)
    public Page<Article> findWithFilter(ArticleFilter filter, Pageable pageable) {
        if (filter == null) {
            return articleRepository.findAll(pageable);
        }

        if (filter.getStatus() != null) {
            return findByStatus(filter.getStatus(), pageable);
        }
        if (filter.getCategoryId() != null) {
            return findByCategoryId(filter.getCategoryId().intValue(), pageable);
        }
        if (filter.getAuthorId() != null) {
            return findByAuthorId(filter.getAuthorId(), pageable);
        }
        if (filter.getFeatured() != null && filter.getFeatured()) {
            return findFeatured(pageable);
        }
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            return searchPublished(filter.getSearch().trim(), pageable);
        }
        if (filter.getIssueId() != null) {
            return findPublishedByIssueId(filter.getIssueId(), pageable);
        }
        if (filter.getTagIds() != null && !filter.getTagIds().isEmpty()) {
            return findPublishedByTagIds(filter.getTagIds().stream().map(Long::intValue).toList(), pageable);
        }

        return articleRepository.findAll(pageable);
    }
}
