package com.uep.pillar.dto;

import com.uep.pillar.model.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMediaInput {
    private String url;
    private String publicId;
    private String altText;
    private MediaType type;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
}
