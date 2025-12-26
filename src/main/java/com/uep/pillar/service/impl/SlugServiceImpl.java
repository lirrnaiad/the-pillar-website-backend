package com.uep.pillar.service.impl;

import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.CategoryRepository;
import com.uep.pillar.repository.PublicationIssueRepository;
import com.uep.pillar.repository.TagRepository;
import com.uep.pillar.service.SlugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
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

        String candidate = base;
        int counter = 1;

        while (exists(candidate, entityClass, excludeId)) {
            counter++;
            candidate = base + "-" + counter;
            if (candidate.length() > 100) {
                // ensure max length while appending counter
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
     * Optimized to reduce database queries when excludeId is provided.
     * 
     * @param slug the slug to check
     * @param entityClass the entity class (Article, Category, Tag, PublicationIssue)
     * @param excludeId optional ID to exclude from check (for updates)
     * @return true if slug exists and conflicts (not the same record)
     */
    private boolean exists(String slug, Class<?> entityClass, Long excludeId) {
        if (entityClass == null) return false;

        String className = entityClass.getSimpleName();
        
        // If excludeId is provided, check if slug belongs to that record first
        // This avoids unnecessary existsBySlug query if it's the same record
        if (excludeId != null) {
            if (isSameRecord(slug, excludeId, className)) {
                return false; // Same record, no conflict
            }
        }

        // Check if slug exists for any record
        return switch (className) {
            case "Article" -> articleRepository.existsBySlug(slug);
            case "Category" -> categoryRepository.existsBySlug(slug);
            case "Tag" -> tagRepository.existsBySlug(slug);
            case "PublicationIssue" -> publicationIssueRepository.existsBySlug(slug);
            default -> false;
        };
    }

    /**
     * Check if the slug belongs to the record with the given excludeId.
     * This allows updating a record without triggering a uniqueness conflict.
     */
    private boolean isSameRecord(String slug, Long excludeId, String className) {
        if (excludeId == null) return false;
        
        try {
            return switch (className) {
                case "Article" -> articleRepository.findBySlug(slug)
                        .map(a -> a.getId() != null && a.getId().equals(excludeId))
                        .orElse(false);
                case "Category" -> categoryRepository.findBySlug(slug)
                        .map(c -> c.getId() != null && c.getId().longValue() == excludeId)
                        .orElse(false);
                case "Tag" -> tagRepository.findBySlug(slug)
                        .map(t -> t.getId() != null && t.getId().longValue() == excludeId)
                        .orElse(false);
                case "PublicationIssue" -> publicationIssueRepository.findBySlug(slug)
                        .map(pi -> pi.getId() != null && pi.getId().equals(excludeId))
                        .orElse(false);
                default -> false;
            };
        } catch (Exception ignored) {
            return false;
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
