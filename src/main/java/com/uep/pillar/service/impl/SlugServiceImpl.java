package com.uep.pillar.service.impl;

import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.CategoryRepository;
import com.uep.pillar.repository.PublicationIssueRepository;
import com.uep.pillar.repository.TagRepository;
import com.uep.pillar.service.SlugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlugServiceImpl implements SlugService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PublicationIssueRepository publicationIssueRepository;

    @Override
    public String generateUniqueSlug(String title, Class<?> entityClass, Long excludeId) {
        String base = sanitize(title);
        if (base.isBlank()) {
            base = "item"; // fallback to non-empty slug
        }

        // Truncate base slug before loop to avoid collision issues
        if (base.length() > 90) {
            base = truncate(base, 90); // Reserve 10 chars for suffix like "-999999"
        }

        String candidate = base;
        int counter = 0; // Start at 0 so first conflict becomes "slug-1"

        while (exists(candidate, entityClass, excludeId)) {
            counter++;
            candidate = base + "-" + counter;
            if (candidate.length() > 100) {
                // If counter makes it too long, truncate base further
                String suffix = "-" + counter;
                candidate = truncate(base, 100 - suffix.length()) + suffix;
            }
        }

        return candidate;
    }

    @Override
    public String sanitize(String input) {
        if (input == null) return "";
        String lower = input.toLowerCase(Locale.ROOT);
        // replace non alphanumeric with hyphen
        String replaced = lower.replaceAll("[^a-z0-9]+", "-");
        // collapse multiple hyphens
        String collapsed = replaced.replaceAll("-+", "-");
        // trim hyphens
        String trimmed = collapsed.replaceAll("^-|-$", "");
        // enforce length limit
        return truncate(trimmed, 100);
    }

    /**
     * Check if a slug exists for the given entity class.
     * 
     * @param slug the slug to check
     * @param entityClass the entity class (Article, Category, Tag, PublicationIssue)
     * @param excludeId optional ID to exclude from check (for updates)
     * @return true if slug exists and conflicts (not the same record)
     */
    private boolean exists(String slug, Class<?> entityClass, Long excludeId) {
        if (entityClass == null) return false;

        String className = entityClass.getSimpleName();
        
        // Check if slug exists, excluding the record with excludeId if provided
        return switch (className) {
            case "Article" -> existsForArticle(slug, excludeId);
            case "Category" -> existsForCategory(slug, excludeId);
            case "Tag" -> existsForTag(slug, excludeId);
            case "PublicationIssue" -> existsForPublicationIssue(slug, excludeId);
            default -> false;
        };
    }

    private boolean existsForArticle(String slug, Long excludeId) {
        if (excludeId == null) {
            return articleRepository.existsBySlug(slug);
        }
        return articleRepository.findBySlug(slug)
                .map(a -> !a.getId().equals(excludeId))
                .orElse(false);
    }

    private boolean existsForCategory(String slug, Long excludeId) {
        if (excludeId == null) {
            return categoryRepository.existsBySlug(slug);
        }
        return categoryRepository.findBySlug(slug)
                .map(c -> !c.getId().equals(excludeId.intValue()))
                .orElse(false);
    }

    private boolean existsForTag(String slug, Long excludeId) {
        if (excludeId == null) {
            return tagRepository.existsBySlug(slug);
        }
        return tagRepository.findBySlug(slug)
                .map(t -> !t.getId().equals(excludeId.intValue()))
                .orElse(false);
    }

    private boolean existsForPublicationIssue(String slug, Long excludeId) {
        if (excludeId == null) {
            return publicationIssueRepository.existsBySlug(slug);
        }
        return publicationIssueRepository.findBySlug(slug)
                .map(pi -> !pi.getId().equals(excludeId))
                .orElse(false);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
