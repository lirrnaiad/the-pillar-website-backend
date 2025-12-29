package com.uep.pillar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing an article category.
 * Categories: News, Feature, Opinion, Sci-Tech, Photos, Cartoons, Videos, Editorial
 * 
 * Note: Uses Integer for ID as categories are a small lookup table (SERIAL in PostgreSQL).
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_slug", columnList = "slug")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Hex color code for UI display (e.g., #E53935).
     * Format: #RRGGBB (7 characters including #).
     * 
     * Validation of hex color format should be performed at the service layer
     * using a regex pattern like: ^#[0-9A-Fa-f]{6}$
     */
    @Column(length = 7)
    private String color;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id != null && Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", slug='" + slug + "'}";
    }
}
