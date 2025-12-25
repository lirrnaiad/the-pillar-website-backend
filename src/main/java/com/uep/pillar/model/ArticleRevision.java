package com.uep.pillar.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a revision/version of an article.
 * Used to track changes and enable rollback functionality.
 */
@Entity
@Table(name = "article_revisions", indexes = {
    @Index(name = "idx_article_revisions_article", columnList = "article_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The article this revision belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /**
     * Title at the time of this revision.
     */
    @Column(length = 255)
    private String title;

    /**
     * Content at the time of this revision.
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * User who made this revision.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revised_by")
    private User revisedBy;

    /**
     * When this revision was created.
     */
    @CreationTimestamp
    @Column(name = "revised_at", updatable = false)
    private LocalDateTime revisedAt;

    /**
     * Optional note describing the changes made.
     */
    @Column(name = "revision_note", length = 255)
    private String revisionNote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleRevision that = (ArticleRevision) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ArticleRevision{id=" + id + ", revisedAt=" + revisedAt + "}";
    }
}
