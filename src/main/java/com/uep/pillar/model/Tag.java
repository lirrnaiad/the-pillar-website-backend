package com.uep.pillar.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing an article tag for flexible categorization.
 * 
 * Note: Uses Integer for ID as tags are a lookup table (SERIAL in PostgreSQL).
 */
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tags_slug", columnList = "slug")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id != null && Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Tag{id=" + id + ", slug='" + slug + "'}";
    }
}
