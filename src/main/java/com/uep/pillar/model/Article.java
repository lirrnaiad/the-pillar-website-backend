package com.uep.pillar.model;

import com.uep.pillar.model.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing an article/post in the publication.
 * Supports SEO metadata, full-text search, and soft deletes.
 */
@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_articles_slug", columnList = "slug"),
    @Index(name = "idx_articles_status", columnList = "status"),
    @Index(name = "idx_articles_author", columnList = "author_id"),
    @Index(name = "idx_articles_category", columnList = "category_id"),
    @Index(name = "idx_articles_published", columnList = "published_at"),
    @Index(name = "idx_articles_active", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    /**
     * SEO-friendly URL slug (unique).
     */
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    /**
     * Full article content (HTML or Markdown).
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Short summary/excerpt of the article.
     */
    @Column(columnDefinition = "TEXT")
    private String excerpt;

    /**
     * Article workflow status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    /**
     * Whether this article is featured on the homepage.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    /**
     * Number of times the article has been viewed.
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    // ==================== RELATIONSHIPS ====================

    /**
     * Author of the article.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Category of the article.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Cover/featured image for the article.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_id")
    private Media cover;

    /**
     * Publication issue this article belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private PublicationIssue issue;

    /**
     * Tags associated with this article.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "article_tags",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ==================== SEO METADATA ====================

    /**
     * Custom SEO title (max 60 chars).
     */
    @Column(name = "meta_title", length = 60)
    private String metaTitle;

    /**
     * Meta description for search engines (max 160 chars).
     */
    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    /**
     * Open Graph image URL for social sharing.
     */
    @Column(name = "og_image", columnDefinition = "TEXT")
    private String ogImage;

    // ==================== TIMESTAMPS ====================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * When the article was published.
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * Soft delete timestamp. If not null, the article is considered deleted.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== HELPER METHODS ====================

    /**
     * Check if the article is soft-deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if the article is published and visible.
     */
    public boolean isPublished() {
        return status == ArticleStatus.PUBLISHED && deletedAt == null;
    }

    /**
     * Add a tag to the article.
     */
    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        tags.add(tag);
    }

    /**
     * Remove a tag from the article.
     */
    public void removeTag(Tag tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return id != null && Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Article{id=" + id + ", slug='" + slug + "', status=" + status + "}";
    }
}
