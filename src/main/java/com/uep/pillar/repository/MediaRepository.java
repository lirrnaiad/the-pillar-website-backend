package com.uep.pillar.repository;

import com.uep.pillar.model.Media;
import com.uep.pillar.model.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Media entity operations.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * Find media by the user who uploaded it.
     * 
     * @param userId the uploader's user ID
     * @param pageable pagination info
     * @return paginated list of media
     */
    Page<Media> findByUploadedById(Long userId, Pageable pageable);

    /**
     * Find media by type (IMAGE, VIDEO, DOCUMENT).
     * 
     * @param type the media type
     * @param pageable pagination info
     * @return paginated list of media
     */
    Page<Media> findByType(MediaType type, Pageable pageable);

    /**
     * Find media by type and uploader.
     * 
     * @param type the media type
     * @param userId the uploader's user ID
     * @param pageable pagination info
     * @return paginated list of media
     */
    Page<Media> findByTypeAndUploadedById(MediaType type, Long userId, Pageable pageable);

    /**
     * Find media by Cloudinary public ID.
     * 
     * @param publicId the Cloudinary public ID
     * @return the media if found
     */
    Optional<Media> findByPublicId(String publicId);

    /**
     * Get all images (for gallery/media library).
     * 
     * @param pageable pagination info
     * @return paginated list of images
     */
    @Query("SELECT m FROM Media m WHERE m.type = 'IMAGE' ORDER BY m.createdAt DESC")
    Page<Media> findAllImages(Pageable pageable);

    /**
     * Get total storage used by a user (sum of file sizes).
     * 
     * @param userId the user ID
     * @return total bytes used
     */
    @Query("SELECT COALESCE(SUM(m.sizeBytes), 0) FROM Media m WHERE m.uploadedBy.id = :userId")
    Long getTotalStorageByUser(@Param("userId") Long userId);

    /**
     * Find recent uploads.
     * 
     * @param limit maximum number of results
     * @return list of recent media
     */
    @Query("SELECT m FROM Media m ORDER BY m.createdAt DESC LIMIT :limit")
    List<Media> findRecentUploads(@Param("limit") int limit);
}

