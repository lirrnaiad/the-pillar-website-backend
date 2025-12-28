package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateMediaInput;
import com.uep.pillar.model.Media;
import com.uep.pillar.model.User;
import com.uep.pillar.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for Media mutations.
 * Handles media upload and deletion.
 */
@Component
@RequiredArgsConstructor
public class MediaMutationResolver extends BaseMutationResolver {

    private final MediaService mediaService;

    /**
     * Upload a new media file.
     *
     * @param input media creation input
     * @return the created media record
     */
    public Media uploadMedia(CreateMediaInput input) {
        // Get current authenticated user as uploader
        User uploader = getCurrentUser();
        if (uploader == null) {
            throw new IllegalStateException("Authentication required to upload media");
        }

        return mediaService.saveMetadata(
            input.getUrl(),
            input.getPublicId(),
            input.getAltText(),
            input.getType(),
            input.getSizeBytes(),
            input.getWidth(),
            input.getHeight(),
            uploader
        );
    }

    /**
     * Delete a media file.
     *
     * @param id the media ID
     * @return true on success
     */
    public Boolean deleteMedia(String id) {
        Long mediaId = parseLongId(id, "Media ID");
        mediaService.delete(mediaId);
        return true;
    }
}
