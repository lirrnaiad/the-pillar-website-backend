package com.uep.pillar.controller;

import com.uep.pillar.dto.UploadResult;
import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.exception.StorageException;
import com.uep.pillar.exception.UnauthorizedException;
import com.uep.pillar.model.Media;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.MediaType;
import com.uep.pillar.service.CloudStorageService;
import com.uep.pillar.service.MediaService;
import com.uep.pillar.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for media uploads backed by Cloudinary.
 *
 * GraphQL is not ideal for multipart uploads; this REST endpoint handles uploads,
 * while GraphQL mutations can be used to update metadata post-upload.
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaUploadController {

    private final CloudStorageService cloudStorageService;
    private final MediaService mediaService;
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") MediaType type,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "folder", required = false) String folder
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }
        String email = auth.getName();
        User uploader = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email=" + email));

        try {
            UploadResult uploadResult = cloudStorageService.uploadFile(file, type, folder);
            Media saved = mediaService.saveMetadata(
                    uploadResult.getUrl(),
                    uploadResult.getPublicId(),
                    altText,
                    type,
                    uploadResult.getSizeBytes(),
                    uploadResult.getWidth(),
                    uploadResult.getHeight(),
                    uploader
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (StorageException e) {
            log.error("Storage error during upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        Media media = mediaService.findById(id);
        try {
            if (media.getPublicId() != null && !media.getPublicId().isBlank()) {
                cloudStorageService.deleteFile(media.getPublicId());
            }
        } catch (StorageException e) {
            log.warn("Cloudinary delete failed for publicId={}: {}", media.getPublicId(), e.getMessage());
            // Proceed with DB deletion regardless
        }
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
