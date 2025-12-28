package com.uep.pillar.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.uep.pillar.dto.UploadResult;
import com.uep.pillar.exception.StorageException;
import com.uep.pillar.model.enums.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Cloud storage service backed by Cloudinary.
 *
 * Handles file uploads, deletions, and transformation URL generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudStorageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:pillar-uploads}")
    private String defaultFolder;

    /**
     * Upload a file to Cloudinary.
     *
     * @param file   multipart file
     * @param type   media type
     * @param folder optional folder; defaults to application property
     * @return upload result with metadata
     */
    public UploadResult uploadFile(MultipartFile file, MediaType type, String folder) {
        validateFile(file, type);

        String resourceType = mapResourceType(type);
        String targetFolder = (folder == null || folder.isBlank()) ? buildFolder(defaultFolder, type) : buildFolder(folder, type);

        @SuppressWarnings("unchecked")
        Map<String, Object> options = (Map<String, Object>) ObjectUtils.asMap(
                "folder", targetFolder,
                "resource_type", resourceType,
                "overwrite", false,
                "use_filename", true,
                "unique_filename", true
        );

        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(file.getBytes(), options);
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        } catch (Exception e) {
            throw new StorageException("Cloudinary upload failed: " + e.getMessage(), e);
        }

        String url = (String) (result.get("secure_url") != null ? result.get("secure_url") : result.get("url"));
        String publicId = (String) result.get("public_id");
        Long bytes = toLong(result.get("bytes"));
        Integer width = toInt(result.get("width"));
        Integer height = toInt(result.get("height"));
        String format = (String) result.get("format");
        String resource = (String) (result.get("resource_type") != null ? result.get("resource_type") : resourceType);

        return UploadResult.builder()
                .url(url)
                .publicId(publicId)
                .sizeBytes(bytes)
                .width(width)
                .height(height)
                .format(format)
                .resourceType(resource)
                .build();
    }

    /**
     * Delete a file from Cloudinary by public ID.
     * Attempts for image, then video, then raw.
     */
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        for (String resourceType : List.of("image", "video", "raw")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> options = (Map<String, Object>) ObjectUtils.asMap("resource_type", resourceType);
                Map<?, ?> res = cloudinary.uploader().destroy(publicId, options);
                Object result = res.get("result");
                if (Objects.equals(result, "ok")) {
                    return;
                }
                // If not found, try next resource type
                if (Objects.equals(result, "not found")) {
                    continue;
                }
            } catch (Exception e) {
                // Log and continue; if all fail, throw
                log.warn("Cloudinary delete failed for resource_type={} publicId={}: {}", resourceType, publicId, e.getMessage());
            }
        }
        throw new StorageException("Failed to delete Cloudinary asset with publicId=" + publicId);
    }

    /**
     * Generate a Cloudinary transformation URL.
     *
     * @param publicId       the asset public ID
     * @param transformations map of transformation key->value, e.g., {"w":"800","h":"600","c":"fill","q":"auto"}
     * @param type            the media type for resource selection
     * @return a secure URL with transformations applied
     */
    public String generateTransformationUrl(String publicId, Map<String, String> transformations, MediaType type) {
        String resourceType = mapResourceType(type);
        String raw = toTransformationString(transformations);
        @SuppressWarnings({"rawtypes"})
        Transformation transformation = new Transformation().rawTransformation(raw);
        return cloudinary.url()
                .secure(true)
                .resourceType(resourceType)
                .transformation(transformation)
                .generate(publicId);
    }

    private String mapResourceType(MediaType type) {
        return switch (type) {
            case IMAGE -> "image";
            case VIDEO -> "video";
            case DOCUMENT -> "raw";
        };
    }

    private String buildFolder(String baseFolder, MediaType type) {
        String typeFolder = switch (type) {
            case IMAGE -> "images";
            case VIDEO -> "videos";
            case DOCUMENT -> "documents";
        };
        String trimmed = baseFolder.endsWith("/") ? baseFolder.substring(0, baseFolder.length() - 1) : baseFolder;
        return trimmed + "/" + typeFolder;
    }

    private void validateFile(MultipartFile file, MediaType type) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String extension = extractExtension(originalFilename);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");

        switch (type) {
            case IMAGE -> {
                long max = 10 * 1024 * 1024L; // 10MB
                if (file.getSize() > max) throw new IllegalArgumentException("Image file too large (max 10MB)");
                Set<String> allowedExt = Set.of("jpg", "jpeg", "png", "gif", "webp");
                Set<String> allowedMime = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
                validateType(extension, contentType, allowedExt, allowedMime);
            }
            case VIDEO -> {
                long max = 50 * 1024 * 1024L; // 50MB
                if (file.getSize() > max) throw new IllegalArgumentException("Video file too large (max 50MB)");
                Set<String> allowedExt = Set.of("mp4", "webm");
                Set<String> allowedMime = Set.of("video/mp4", "video/webm");
                validateType(extension, contentType, allowedExt, allowedMime);
            }
            case DOCUMENT -> {
                long max = 5 * 1024 * 1024L; // 5MB
                if (file.getSize() > max) throw new IllegalArgumentException("Document file too large (max 5MB)");
                Set<String> allowedExt = Set.of("pdf", "doc", "docx");
                Set<String> allowedMime = Set.of("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                validateType(extension, contentType, allowedExt, allowedMime);
            }
        }
    }

    private void validateType(String extension, String contentType, Set<String> allowedExt, Set<String> allowedMime) {
        if (!allowedExt.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension: " + extension);
        }
        if (!allowedMime.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid MIME type: " + contentType);
        }
    }

    private String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.')
                ;
        if (idx < 0 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1);
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }

    private String toTransformationString(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append('_').append(e.getValue());
        }
        return sb.toString();
    }
}
