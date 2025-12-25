package com.uep.pillar.model;

import com.uep.pillar.model.enums.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a media file stored in Cloudinary.
 * Stores metadata and URL, actual file is in cloud storage.
 */
@Entity
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full URL to the media file in Cloudinary.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    /**
     * Cloudinary public ID for transformations and deletion.
     */
    @Column(name = "public_id", length = 255)
    private String publicId;

    /**
     * Alt text for accessibility and SEO.
     */
    @Column(name = "alt_text", length = 255)
    private String altText;

    /**
     * Type of media: IMAGE, VIDEO, or DOCUMENT.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MediaType type;

    /**
     * File size in bytes.
     */
    @Column(name = "size_bytes")
    private Long sizeBytes;

    /**
     * Image/video width in pixels.
     */
    private Integer width;

    /**
     * Image/video height in pixels.
     */
    private Integer height;

    /**
     * User who uploaded this media.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

