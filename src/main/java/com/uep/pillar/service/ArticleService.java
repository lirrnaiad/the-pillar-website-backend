package com.uep.pillar.service;

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

        return articleRepository.save(builder.build());
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
        return articleRepository.save(article);
    }

    @Transactional
    public void softDelete(Long id) {
        // ensure exists first for consistent errors
        Article existing = findById(id);
        articleRepository.softDelete(existing.getId(), LocalDateTime.now());
    }

    @Transactional
    public void restore(Long id) {
        // verify it exists possibly including deleted
        articleRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
        articleRepository.restore(id);
    }

    @Transactional
    public Article setFeatured(Long id, boolean featured) {
        Article article = findById(id);
        article.setFeatured(featured);
        return articleRepository.save(article);
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
        return articleRepository.save(article);
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
        return articleRepository.save(article);
    }

    @Transactional
    public Article reject(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.PENDING_REVIEW) {
            throw new InvalidStatusTransitionException("Only PENDING_REVIEW can be rejected");
        }
        article.setStatus(ArticleStatus.REJECTED);
        return articleRepository.save(article);
    }

    @Transactional
    public Article archive(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new InvalidStatusTransitionException("Only PUBLISHED can be archived");
        }
        article.setStatus(ArticleStatus.ARCHIVED);
        return articleRepository.save(article);
    }

    @Transactional
    public Article revertToDraft(Long id) {
        Article article = findById(id);
        if (article.getStatus() != ArticleStatus.PENDING_REVIEW
                && article.getStatus() != ArticleStatus.REJECTED) {
            throw new InvalidStatusTransitionException("Only PENDING_REVIEW or REJECTED can be reverted to DRAFT");
        }
        article.setStatus(ArticleStatus.DRAFT);
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Page<Article> listPublished(Pageable pageable) {
        return articleRepository.findAllPublished(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> searchPublished(String query, Pageable pageable) {
        return articleRepository.search(query, pageable);
    }

    @Transactional
    public Article addTagsBySlugs(Long articleId, Collection<String> slugs) {
        Article article = findById(articleId);
        List<Tag> tags = tagRepository.findBySlugIn(slugs);
        for (Tag t : tags) article.addTag(t);
        return articleRepository.save(article);
    }

    @Transactional
    public Article removeTagsBySlugs(Long articleId, Collection<String> slugs) {
        Article article = findById(articleId);
        List<Tag> tags = tagRepository.findBySlugIn(slugs);
        for (Tag t : tags) article.removeTag(t);
        return articleRepository.save(article);
    }
}
