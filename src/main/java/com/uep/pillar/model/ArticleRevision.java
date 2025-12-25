package com.uep.pillar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a revision/version of an article.
 * Used to track changes and enable rollback functionality.
 */
@Entity
@Table(name = "article_revisions")
@Data
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
}

