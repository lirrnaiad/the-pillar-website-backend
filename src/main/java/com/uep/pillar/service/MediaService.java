package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Media;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.MediaType;
import com.uep.pillar.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MediaRepository mediaRepository;
    /**
     * Optional Cloud storage service for deleting assets from Cloudinary before DB deletion.
     */
    @Autowired(required = false)
    private CloudStorageService cloudStorageService;

    @Transactional(readOnly = true)
    public Media findById(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media", id));
    }

    @Transactional(readOnly = true)
    public Optional<Media> findByPublicId(String publicId) {
        return mediaRepository.findByPublicId(publicId);
    }

    @Transactional
    public Media saveMetadata(String url, String publicId, String altText,
                              MediaType type, Long sizeBytes, Integer width, Integer height,
                              User uploadedBy) {
        // Validate required fields
        // Note: publicId is required for Cloudinary integration (per ARCHITECTURE.md).
        // Cloudinary always provides a publicId, which is needed for transformations and deletion.
        // The database schema allows null for flexibility, but the service enforces this business rule.
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("publicId is required for Cloudinary media");
        }
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("url is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        
        // Validate logical constraints
        if (sizeBytes != null && sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must be non-negative");
        }
        if (width != null && width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height != null && height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
        
        Media media = Media.builder()
                .url(url)
                .publicId(publicId)
                .altText(altText)
                .type(type)
                .sizeBytes(sizeBytes)
                .width(width)
                .height(height)
                .uploadedBy(uploadedBy)
                .build();
        return mediaRepository.save(media);
    }

    @Transactional
    public Media update(Long id, String altText) {
        Media existing = findById(id);
        if (altText != null) existing.setAltText(altText);
        return mediaRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Media existing = findById(id);
        // Attempt to delete from Cloudinary first, if available
        if (cloudStorageService != null && existing.getPublicId() != null && !existing.getPublicId().trim().isEmpty()) {
            try {
                cloudStorageService.deleteFile(existing.getPublicId());
            } catch (Exception e) {
                // Log and continue with DB deletion
                log.warn("Failed to delete Cloudinary asset publicId={}: {}", existing.getPublicId(), e.getMessage());
            }
        }
        mediaRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Page<Media> listByUploader(Long userId, Pageable pageable) {
        return mediaRepository.findByUploadedById(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Media> listByType(MediaType type, Pageable pageable) {
        return mediaRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.List<Media> findAll() {
        return mediaRepository.findAll();
    }
}
