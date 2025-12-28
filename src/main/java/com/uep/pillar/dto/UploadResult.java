package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the result of a media upload to Cloudinary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private String url;
    private String publicId;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String format;
    private String resourceType;
}
