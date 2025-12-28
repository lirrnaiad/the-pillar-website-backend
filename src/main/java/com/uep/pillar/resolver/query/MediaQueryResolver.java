package com.uep.pillar.resolver.query;

import com.uep.pillar.model.Media;
import com.uep.pillar.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GraphQL Query Resolver for Media queries.
 */
@Component
@RequiredArgsConstructor
public class MediaQueryResolver {

    private final MediaService mediaService;

    /**
     * Get all media items.
     */
    public java.util.List<Media> media() {
        return mediaService.findAll();
    }

    /**
     * Get a single media item by ID.
     */
    public Media mediaItem(String id) {
        try {
            Long mediaId = Long.parseLong(id);
            return mediaService.findById(mediaId);
        } catch (NumberFormatException | com.uep.pillar.exception.ResourceNotFoundException e) {
            return null;
        }
    }
}
