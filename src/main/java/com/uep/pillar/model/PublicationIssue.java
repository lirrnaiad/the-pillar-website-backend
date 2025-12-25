package com.uep.pillar.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a publication issue/volume for grouping articles.
 */
@Entity
@Table(name = "publication_issues", indexes = {
    @Index(name = "idx_publication_issues_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    /**
     * Cover image URL for the issue.
     */
    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Publication date of this issue.
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationIssue that = (PublicationIssue) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PublicationIssue{id=" + id + ", slug='" + slug + "'}";
    }
}
